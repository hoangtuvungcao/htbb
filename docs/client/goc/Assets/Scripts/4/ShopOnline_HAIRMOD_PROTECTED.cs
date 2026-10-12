using System;
using UnityEngine;

public class ShopOnline_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public Scroll_HAIRMOD_PROTECTED scl;

	public Scroll_HAIRMOD_PROTECTED cmrShowTxt;

	public static mVector_HAIRMOD_PROTECTED ALL_ITEM = new mVector_HAIRMOD_PROTECTED();

	public static string[] tile = new string[6] { "Chợ", "Tìm", "Đang bán", "Hành trang", "Kho", "Bid" };

	public static short[] nPage = new short[6];

	public static short[] indexPage = new short[6];

	public static string[][] tileFind = new string[3][]
	{
		new string[5] { "Loại:", "Level:", "Phẩm:", "Màu:", "Cộng:" },
		new string[1] { "N.vật" },
		new string[0]
	};

	public static string[] nameItem = new string[19]
	{
		"áo", "Quần", "Nón", "Kiếm", "Đao", "Bút", "Búa", "Cung", "Nhẫn", "Dây chuyền",
		"Giày", "Găng tay", "Ngọc bội", "Giáp linh thú", "Hộ uyển", "Nón linh thú", "Bàn đạp", "Yên cương", "Phi phong"
	};

	public static sbyte[] ID_TYPE_ITEM = new sbyte[19]
	{
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
		10, 11, 12, 14, 15, 16, 17, 18, 19
	};

	public static string[] Pham = new string[5] { "Nhất phẩm", "Nhị phẩm", "Tam phẩm", "Tứ phẩm", "Khác" };

	public static string[] Color = new string[4] { "Trắng", "Xanh", "Đỏ", "Hoàn mỹ" };

	public static short level;

	public static short wStr;

	public static string pham = string.Empty;

	public static string mau = string.Empty;

	public int select;

	public int selectTxt;

	public int x;

	public int y;

	public int w0;

	public int h0;

	public int xShowTxt;

	public int yShowTxt;

	public int wShowTxt;

	public int hShowTxt;

	private int countL;

	private int indexMainTile;

	private int idType;

	private int idLevel = 1;

	private int idPham;

	private int idColor;

	private int idPlus;

	public Command_HAIRMOD_PROTECTED cmdDoNothing;

	public Command_HAIRMOD_PROTECTED cmdSell;

	public Command_HAIRMOD_PROTECTED cmdBuy;

	public Command_HAIRMOD_PROTECTED cmdMenu;

	public Command_HAIRMOD_PROTECTED cmdFind;

	public Command_HAIRMOD_PROTECTED cmdLastLeft;

	public Command_HAIRMOD_PROTECTED cmdLastCenter;

	public Command_HAIRMOD_PROTECTED cmdShowText;

	public MyScreen_HAIRMOD_PROTECTED lastScreen;

	private bool isSelectTypeFind = true;

	private bool isFocusMainTab;

	private sbyte typeSearch;

	public string[] arrInfo = new string[1] { string.Empty };

	public string nameChar = string.Empty;

	public bool isShowText;

	private int xRunL;

	private int xRunR;

	private Image_HAIRMOD_PROTECTED img;

	private sbyte[] arrayKhamNgoc;

	public int wShowLv = 110;

	public string price = "10.000.000.000";

	public string strLv = string.Empty;

	public static long money = 0L;

	public ShopOnline_HAIRMOD_PROTECTED()
	{
		cmdDoNothing = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdDoNothing.action = delegate
		{
		};
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
		ALL_ITEM.addElement(new mVector_HAIRMOD_PROTECTED());
	}

	public void reset()
	{
		select = -1;
		selectTxt = 0;
		level = 0;
		pham = string.Empty;
		mau = string.Empty;
		indexMainTile = 1;
		isSelectTypeFind = true;
		isFocusMainTab = !Canvas_HAIRMOD_PROTECTED.isSmartPhone;
		idColor = (idPham = (idType = 0));
		idLevel = 1;
		isShowText = false;
		arrInfo = new string[1] { string.Empty };
		scl.clear();
		cmrShowTxt.clear();
		setCmdLeft();
	}

	public void setShowTxt(string[] str)
	{
		arrInfo = new string[1] { string.Empty };
		arrInfo = str;
		isShowText = true;
	}

	public void paintNameItemSelect(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, string str, MFont_HAIRMOD_PROTECTED f, int id)
	{
		int num = 100;
		g.drawImage(Res_HAIRMOD_PROTECTED.imgBar, x + num + ((select == id) ? countL : 0), y + 7, 3);
		g.drawRegion(Res_HAIRMOD_PROTECTED.imgBar, 0, 0, 11, 7, 2, x - ((select == id) ? countL : 0), y + 7, 3);
		f.drawString(g, str, x + num / 2, y, 2);
	}

	public override void switchToMe()
	{
		init();
		base.switchToMe();
	}

	public override void switchToMe(MyScreen_HAIRMOD_PROTECTED scr)
	{
		lastScreen = scr;
		init();
		base.switchToMe(scr);
	}

	public override void init()
	{
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			tileFind[1][0] = "Nhân vật";
		}
		if (img == null)
		{
			try
			{
				img = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/02");
			}
			catch (Exception)
			{
			}
		}
		int num = Canvas_HAIRMOD_PROTECTED.w - 6;
		int num2 = Canvas_HAIRMOD_PROTECTED.h - 30;
		if (num > 320)
		{
			num = 320;
		}
		if (num2 > 240)
		{
			num2 = 240;
		}
		w0 = num;
		h0 = num2;
		x = Canvas_HAIRMOD_PROTECTED.w / 2 - w0 / 2;
		y = Canvas_HAIRMOD_PROTECTED.h / 2 - h0 / 2 - 10;
		if (y < 8)
		{
			y = 8;
		}
		wShowTxt = Canvas_HAIRMOD_PROTECTED.w - 40;
		if (wShowTxt > 200)
		{
			wShowTxt = 200;
		}
		hShowTxt = Canvas_HAIRMOD_PROTECTED.h - 80;
		if (hShowTxt > 180)
		{
			hShowTxt = 180;
		}
		xShowTxt = Canvas_HAIRMOD_PROTECTED.w / 2 - wShowTxt / 2;
		yShowTxt = Canvas_HAIRMOD_PROTECTED.h / 2 - hShowTxt / 2;
		if (yShowTxt < 5)
		{
			yShowTxt = 5;
		}
		for (int i = 0; i < tileFind[0].Length; i++)
		{
			if (wStr < MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(tileFind[0][i]))
			{
				wStr = (short)MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(tileFind[0][i]);
			}
		}
		wStr += 5;
		select = -1;
		isFocusMainTab = !Canvas_HAIRMOD_PROTECTED.isSmartPhone;
		isShowText = false;
		idColor = (idPham = (idType = 0));
		idLevel = 1;
		center = new Command_HAIRMOD_PROTECTED((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? "Chọn" : string.Empty);
		center.action = delegate
		{
			doselect();
		};
		cmdFind = new Command_HAIRMOD_PROTECTED("Tìm");
		cmdFind.action = delegate
		{
			doFind();
			left = cmdMenu;
		};
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		right.action = delegate
		{
			if (isShowText)
			{
				isShowText = false;
				setCmdLeft();
			}
			else
			{
				lastScreen.switchToMe();
				reset();
			}
		};
		cmdBuy = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdBuy.action = delegate
		{
		};
		cmdSell = new Command_HAIRMOD_PROTECTED("Menu");
		cmdSell.action = delegate
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			doCmdSell(mVector_HAIRMOD_PROTECTED3);
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 0);
		};
		cmdMenu = new Command_HAIRMOD_PROTECTED("Menu");
		cmdMenu.action = delegate
		{
			isShowText = false;
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Mua")
			{
				action = delegate
				{
					doAcceptBuy(1, (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select));
				}
			};
			command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Tìm")
			{
				action = delegate
				{
					reset();
					typeSearch = 0;
					left = cmdFind;
				}
			};
			command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Tìm theo người bán")
			{
				action = delegate
				{
					reset();
					typeSearch = 1;
					left = cmdFind;
				}
			};
			if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0)
			{
				command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Kết quả");
			}
			command_HAIRMOD_PROTECTED.action = delegate
			{
				isSelectTypeFind = false;
				select = -1;
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
		};
		cmdShowText = new Command_HAIRMOD_PROTECTED("Thông tin ");
		cmdShowText.action = delegate
		{
			doView();
		};
		cmdLastCenter = (cmdLastLeft = cmdDoNothing);
		scl = new Scroll_HAIRMOD_PROTECTED();
		scl.clear();
		cmrShowTxt = new Scroll_HAIRMOD_PROTECTED();
		cmrShowTxt.clear();
		setCmdLeft();
		base.init();
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			setCmdLeft();
			h0 -= 10;
		}
	}

	public void doCmdSell(mVector_HAIRMOD_PROTECTED menu)
	{
		if (isFocusMainTab)
		{
			return;
		}
		isShowText = false;
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Bán");
		command_HAIRMOD_PROTECTED.action = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED ok = delegate
			{
				try
				{
					string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
					int price = int.Parse(text);
					Canvas_HAIRMOD_PROTECTED.endDlg();
					if (price < 0)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể bán với giá này");
					}
					else
					{
						ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
						if (ItemInInventory_HAIRMOD_PROTECTED.isModelClothe(item.getTemplate().attb[9]) || item.dayUse > 0)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể bán vật phẩm này");
						}
						else
						{
							ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
							{
								GameService_HAIRMOD_PROTECTED.gI().doRequestSelItem(0, item.itemID, price, 0);
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi thông tin bán ");
							};
							Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn đặt bán vật phẩm này với giá " + price + "  không?", yesAction);
						}
					}
				}
				catch (Exception)
				{
					Canvas_HAIRMOD_PROTECTED.endDlg();
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
				}
			};
			Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập giá bán", ok, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 10, true);
			Canvas_HAIRMOD_PROTECTED.inputDlg.show();
		};
		menu.addElement(command_HAIRMOD_PROTECTED);
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doSell();
		};
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Bán đấu giá");
		command_HAIRMOD_PROTECTED2.action = action;
		menu.addElement(command_HAIRMOD_PROTECTED2);
	}

	public void doAcceptBuy(int new_search, ItemInInventory_HAIRMOD_PROTECTED item)
	{
		if (item.nameCharSell.Equals(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name))
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể mua vật phẩm do mình đăng bán");
			return;
		}
		ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
		{
			GameService_HAIRMOD_PROTECTED.gI().doBuyItemShopOnline(select, new_search);
			Canvas_HAIRMOD_PROTECTED.endDlg();
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
		};
		Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn mua vật phẩm này không ?", yesAction);
	}

	protected void doSort()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doSortBuyPrice();
		};
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Theo giá ");
		command_HAIRMOD_PROTECTED.action = action;
		mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			doSortBuyLevel();
		};
		command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Theo level");
		command_HAIRMOD_PROTECTED.action = action2;
	}

	protected void doSortBuyLevel()
	{
	}

	protected void doSortBuyPrice()
	{
	}

	protected void doView()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile);
		if ((isFocusMainTab && !Canvas_HAIRMOD_PROTECTED.isSmartPhone) || mVector_HAIRMOD_PROTECTED2.size() == 0)
		{
			return;
		}
		selectTxt = 0;
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
		if (select >= 0 && select < mVector_HAIRMOD_PROTECTED2.size())
		{
			itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(select);
			if (!itemInInventory_HAIRMOD_PROTECTED.isEnoughData)
			{
				itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
				GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(itemInInventory_HAIRMOD_PROTECTED.itemID, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
			}
		}
		string[] showTxt = new string[1] { string.Empty };
		if (itemInInventory_HAIRMOD_PROTECTED != null)
		{
			if (itemInInventory_HAIRMOD_PROTECTED.totalKham > 0)
			{
				arrayKhamNgoc = new sbyte[itemInInventory_HAIRMOD_PROTECTED.totalKham];
				for (int i = 0; i < itemInInventory_HAIRMOD_PROTECTED.totalKham; i++)
				{
					arrayKhamNgoc[i] = -1;
				}
				sbyte b = 0;
				for (int j = 0; j < itemInInventory_HAIRMOD_PROTECTED.allAttribute.size(); j++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED = (InfoAttributeItem_HAIRMOD_PROTECTED)itemInInventory_HAIRMOD_PROTECTED.allAttribute.elementAt(j);
					if (infoAttributeItem_HAIRMOD_PROTECTED.getColorPaint(false) == 3)
					{
						for (int k = 0; k < itemInInventory_HAIRMOD_PROTECTED.numKham; k++)
						{
							arrayKhamNgoc[b++] = Res_HAIRMOD_PROTECTED.idNgocKham[infoAttributeItem_HAIRMOD_PROTECTED.getID() - 10];
						}
						break;
					}
				}
			}
			showTxt = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(itemInInventory_HAIRMOD_PROTECTED.getDescription(false), wShowTxt - 10);
		}
		setShowTxt(showTxt);
	}

	protected void doSell()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile);
		if (select >= 0 && select < mVector_HAIRMOD_PROTECTED2.size())
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(select);
			if (ItemInInventory_HAIRMOD_PROTECTED.isModelClothe(itemInInventory_HAIRMOD_PROTECTED.getTemplate().attb[9]) || itemInInventory_HAIRMOD_PROTECTED.dayUse > 0)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể bán vật phẩm này");
			}
			else
			{
				popupBuyItem(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
	}

	public void doAcceptSell()
	{
	}

	public void paintFillLine(MyGraphics_HAIRMOD_PROTECTED g, int x, int y1, int y2)
	{
		g.setColor(9605778);
		g.fillRect(x, y1, w0 + 2, 1);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (lastScreen != null)
		{
			lastScreen.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, x, y, w0, h0);
		g.setColor(7961465);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 51, y + 7, 102, 22);
		g.setColor(isFocusMainTab ? 30611 : 2368548);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 50, y + 8, 100, 20);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, tile[indexMainTile], Canvas_HAIRMOD_PROTECTED.w / 2, y + 10, 2);
		g.drawImage(Res_HAIRMOD_PROTECTED.imgBar, Canvas_HAIRMOD_PROTECTED.hw - 40 + xRunL, y + 17, 3);
		g.drawRegion(Res_HAIRMOD_PROTECTED.imgBar, 0, 0, 11, 7, 2, Canvas_HAIRMOD_PROTECTED.hw + 40 + xRunR, y + 17, 3);
		MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, money + "$", x + w0 - 2, y + h0 - 12, MFont_HAIRMOD_PROTECTED.RIGHT);
		int num = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 10;
		int num2 = 0;
		if (indexMainTile == 1)
		{
			num = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 10;
			num2 = y + 40;
			if (isSelectTypeFind)
			{
				scl.setStyle(tileFind[0].Length, num, x + 6, y + 40, w0, h0 - 40, true, 0);
				scl.setClip(g, x + 6, y + 40, w0 - 12, h0 - 55);
				for (int i = 0; i < tileFind[typeSearch].Length; i++)
				{
					MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, tileFind[typeSearch][i], x + 6, num2 + i * num, 0);
				}
				string[][] array = new string[2][]
				{
					new string[5]
					{
						nameItem[idType],
						idLevel + string.Empty,
						Pham[idPham],
						Color[idColor],
						idPlus + string.Empty
					},
					new string[1] { (!nameChar.Equals(string.Empty)) ? nameChar : "Nhập tên" }
				};
				int num3 = idColor;
				if (idColor == 1)
				{
					num3 = 3;
				}
				else if (idColor == 3)
				{
					num3 = 1;
				}
				for (int j = 0; j < array[typeSearch].Length; j++)
				{
					paintNameItemSelect(g, (Canvas_HAIRMOD_PROTECTED.w / 2 - 40 > x + 6 + wStr) ? (Canvas_HAIRMOD_PROTECTED.w / 2 - 40) : (x + 6 + wStr), num2 + j * num, array[typeSearch][j], MFont_HAIRMOD_PROTECTED.normalFont[(j == 3) ? num3 : 0], j);
				}
			}
			else
			{
				paintItem(g, (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(1), true);
			}
		}
		else
		{
			paintItem(g, (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile), false);
		}
		if (isShowText)
		{
			paintShowTxt(g);
		}
		base.paint(g);
	}

	public void paintItem(MyGraphics_HAIRMOD_PROTECTED g, mVector_HAIRMOD_PROTECTED vt, bool isShopBuy)
	{
		int num = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT * 3 + 10;
		scl.setStyle(vt.size(), num, x + 6, y + 40, w0, h0 - 40, true, 0);
		scl.setClip(g, x + 6, y + 40, w0 - 12, h0 - 55);
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = null;
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
		bool flag = false;
		for (int i = 0; i < vt.size(); i++)
		{
			itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)vt.elementAt(i);
			int num2 = y + 40 + i * num;
			itemTemplate_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (num2 + num - scl.cmy < y + 30 || num2 - scl.cmy > y + h0)
			{
				continue;
			}
			if (select == i)
			{
				g.setColor(34949);
				g.fillRect(x - 1, num2 - 5, w0 - 2, num);
				flag = true;
			}
			if (itemInInventory_HAIRMOD_PROTECTED.prizeBet > 0)
			{
				g.drawImage(img, x + 12, num2 + 20, 0);
			}
			paintFillLine(g, x, num2 - 5, num2);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgGrid, x + 6, num2, 0);
			string[] name = itemInInventory_HAIRMOD_PROTECTED.getName();
			for (int j = 0; j < name.Length; j++)
			{
				sbyte b = (sbyte)(name[j][0] - 48);
				sbyte startIndex = 1;
				if (!WindowInfoScr_HAIRMOD_PROTECTED.isDegit(name[j][0]))
				{
					b = 0;
					startIndex = 0;
				}
				MFont_HAIRMOD_PROTECTED.normalFont[(b < 5) ? b : 0].drawString(g, name[j].Substring(startIndex), 30 + x, num2 + j * MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2, 0);
			}
			GameData_HAIRMOD_PROTECTED.paintIcon(g, itemTemplate_HAIRMOD_PROTECTED.idIcon, 16 + x, num2 + 8);
			if (select == i && i > 1)
			{
				paintLevelAndPrice(g, x + 10, num2 - 5 - (num + 5), isShopBuy, true, itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		paintFillLine(g, x, y + 40 + vt.size() * num - 5, 1);
		if (flag && (select == 0 || select == 1))
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)vt.elementAt(select);
			paintLevelAndPrice(g, x + 10, y + 32 - num + select * num, isShopBuy, true, itemInInventory_HAIRMOD_PROTECTED);
		}
	}

	public void paintShowTxt(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.setColor(16774720);
		g.fillRect(xShowTxt - 1, yShowTxt - 1, wShowTxt + 2, hShowTxt + 2);
		g.setColor(25695);
		g.fillRect(xShowTxt, yShowTxt, wShowTxt, hShowTxt);
		cmrShowTxt.setStyle(arrInfo.Length, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2, xShowTxt, yShowTxt, wShowTxt, hShowTxt, true, 0);
		cmrShowTxt.setClip(g, xShowTxt, yShowTxt, wShowTxt, hShowTxt);
		int num = 0;
		for (int i = 0; i < arrInfo.Length; i++)
		{
			if (i == 1 && arrayKhamNgoc != null && arrayKhamNgoc.Length != 0)
			{
				for (int j = 0; j < arrayKhamNgoc.Length; j++)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgKham, xShowTxt + 12 + j * 20, yShowTxt + 12 + num, 3);
					if (arrayKhamNgoc[j] != -1)
					{
					}
				}
				num += 18;
			}
			if (!arrInfo[i].Equals(string.Empty))
			{
				sbyte b = (sbyte)(arrInfo[i][0] - 48);
				sbyte startIndex = 1;
				if (!WindowInfoScr_HAIRMOD_PROTECTED.isDegit(arrInfo[i][0]))
				{
					b = 0;
					startIndex = 0;
				}
				MFont_HAIRMOD_PROTECTED.normalFont[(b < 5) ? b : 0].drawString(g, arrInfo[i].Substring(startIndex), xShowTxt + 4, yShowTxt + 4 + i * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2) + num, 0);
			}
		}
	}

	public override void update()
	{
		if (lastScreen != null)
		{
			lastScreen.update();
		}
		if (countL > 0)
		{
			countL--;
		}
		if (countL <= 0)
		{
			countL = 5;
		}
		if (xRunL < 0)
		{
			xRunL++;
		}
		if (xRunR > 0)
		{
			xRunR--;
		}
		if (Canvas_HAIRMOD_PROTECTED.currentDialog == null && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			if (isShowText)
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = cmrShowTxt.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED.isDowning || scrollResult_HAIRMOD_PROTECTED.isFinish)
				{
					selectTxt = scrollResult_HAIRMOD_PROTECTED.selected;
				}
				cmrShowTxt.updatecm();
			}
			else
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED2 = scl.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED2.isDowning || scrollResult_HAIRMOD_PROTECTED2.isFinish)
				{
					select = scrollResult_HAIRMOD_PROTECTED2.selected;
				}
				if (Canvas_HAIRMOD_PROTECTED.isPointer(x, y + 30, w0, h0 - 30) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					if (scrollResult_HAIRMOD_PROTECTED2.selected != -1 && center != null)
					{
						center.perform();
					}
				}
				scl.updatecm();
			}
		}
		base.update();
	}

	public void updatePoint()
	{
		if (indexMainTile == 0 && Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer((Canvas_HAIRMOD_PROTECTED.w / 2 - 40 > x + 6 + wStr) ? (Canvas_HAIRMOD_PROTECTED.w / 2 - 40) : (x + 6 + wStr), y + 30, 100, 4 * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 10)))
		{
			int num = -1;
			num = (Canvas_HAIRMOD_PROTECTED.py - (y + 30)) / (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 10);
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			setCmdLeft();
			if (num >= 0 && num <= 3)
			{
				select = num;
				if (center != null)
				{
					center.perform();
				}
			}
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			return;
		}
		isFocusMainTab = false;
		Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
		if (Canvas_HAIRMOD_PROTECTED.py >= y && Canvas_HAIRMOD_PROTECTED.py <= y + 25)
		{
			if (Canvas_HAIRMOD_PROTECTED.px > x && Canvas_HAIRMOD_PROTECTED.px < Canvas_HAIRMOD_PROTECTED.w / 2)
			{
				indexMainTile--;
				if (indexMainTile < 0)
				{
					indexMainTile = tile.Length - 1;
				}
				xRunL = -5;
				scl.clear();
				select = -1;
				setCmdLeft();
			}
			else if (Canvas_HAIRMOD_PROTECTED.px < Canvas_HAIRMOD_PROTECTED.w / 2 + w0 / 2 && Canvas_HAIRMOD_PROTECTED.px > Canvas_HAIRMOD_PROTECTED.w / 2)
			{
				indexMainTile++;
				if (indexMainTile > tile.Length - 1)
				{
					indexMainTile = 0;
				}
				xRunR = 5;
				scl.clear();
				left = cmdLastLeft;
				select = -1;
				setCmdLeft();
			}
		}
		if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0 && select == -1)
		{
			select = 0;
		}
		setCmdLeft();
	}

	public override void updateKey()
	{
		if (isFocusMainTab)
		{
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				indexMainTile--;
				if (indexMainTile < 0)
				{
					indexMainTile = tile.Length - 1;
				}
				xRunL = -5;
				scl.clear();
				left = cmdLastLeft;
				select = -1;
				setCmdLeft();
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				indexMainTile++;
				if (indexMainTile > tile.Length - 1)
				{
					indexMainTile = 0;
				}
				xRunR = 5;
				scl.clear();
				left = cmdLastLeft;
				select = -1;
				setCmdLeft();
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
				select = 0;
				isFocusMainTab = false;
				setCmdLeft();
			}
		}
		else
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile);
			int num = mVector_HAIRMOD_PROTECTED2.size();
			if (isSelectTypeFind && indexMainTile == 1)
			{
				if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
					select--;
					if (select <= -1)
					{
						select = -1;
						isFocusMainTab = true;
						setCmdLeft();
					}
					scl.moveTo(select * scl.ITEM_SIZE);
				}
				else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
					select++;
					if (select > tileFind[typeSearch].Length - 1)
					{
						select = 0;
					}
					scl.moveTo(select * scl.ITEM_SIZE);
				}
			}
			else if (!isFocusMainTab)
			{
				if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
					if (isShowText)
					{
						selectTxt--;
						if (selectTxt < 0)
						{
							selectTxt = arrInfo.Length - 1;
						}
						cmrShowTxt.moveTo(selectTxt * cmrShowTxt.ITEM_SIZE);
					}
					else
					{
						select--;
						if (select <= -1)
						{
							select = -1;
							isFocusMainTab = true;
						}
						setCmdLeft();
						scl.moveTo(select * scl.ITEM_SIZE);
					}
				}
				else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
					if (isShowText)
					{
						selectTxt++;
						if (selectTxt > arrInfo.Length - 1)
						{
							selectTxt = 0;
						}
						cmrShowTxt.moveTo(selectTxt * cmrShowTxt.ITEM_SIZE);
					}
					else
					{
						select++;
						if (select > num - 1)
						{
							select = 0;
						}
						setCmdLeft();
						scl.moveTo(select * scl.ITEM_SIZE);
					}
				}
			}
		}
		base.updateKey();
		if (Canvas_HAIRMOD_PROTECTED.currentDialog == null && !Canvas_HAIRMOD_PROTECTED.menu.showMenu && !isShowText)
		{
			updatePoint();
		}
	}

	public void paintLevelAndPrice(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, bool isShopBuy, bool isBuy, ItemInInventory_HAIRMOD_PROTECTED item)
	{
	}

	public void doFind()
	{
		isSelectTypeFind = false;
		select = -1;
		if (typeSearch == 0)
		{
			GameService_HAIRMOD_PROTECTED.gI().doSearchItemSell(idLevel, ID_TYPE_ITEM[idType], idColor, idPham + 1, idPlus);
		}
		else if (typeSearch == 1)
		{
			GameService_HAIRMOD_PROTECTED.gI().doSearchByName(nameChar);
		}
	}

	public void doselect()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == PageScr_HAIRMOD_PROTECTED.gI())
		{
			return;
		}
		if (indexMainTile == 1)
		{
			if (isSelectTypeFind)
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = null;
				if (typeSearch == 0)
				{
					switch (select)
					{
					case 0:
					{
						for (int j = 0; j < nameItem.Length; j++)
						{
							command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(nameItem[j]);
							command_HAIRMOD_PROTECTED.action = delegate
							{
								idType = Canvas_HAIRMOD_PROTECTED.menu.selected;
							};
							mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
						}
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
						break;
					}
					case 1:
					{
						ActionPerform_HAIRMOD_PROTECTED ok = delegate
						{
							try
							{
								string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
								idLevel = int.Parse(text);
								Canvas_HAIRMOD_PROTECTED.endDlg();
								if (idLevel < 0)
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số lớn hơn 0 ");
									idLevel = 0;
								}
							}
							catch (Exception)
							{
								Canvas_HAIRMOD_PROTECTED.endDlg();
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
							}
						};
						Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập số", ok, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 3, true);
						Canvas_HAIRMOD_PROTECTED.inputDlg.show();
						break;
					}
					case 2:
					{
						for (int k = 0; k < Pham.Length; k++)
						{
							command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(Pham[k]);
							command_HAIRMOD_PROTECTED.action = delegate
							{
								idPham = Canvas_HAIRMOD_PROTECTED.menu.selected;
							};
							mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
						}
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
						break;
					}
					case 3:
					{
						for (int l = 0; l < Color.Length; l++)
						{
							command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(Color[l]);
							command_HAIRMOD_PROTECTED.action = delegate
							{
								idColor = Canvas_HAIRMOD_PROTECTED.menu.selected;
							};
							mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
						}
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
						break;
					}
					case 4:
					{
						for (int i = 0; i < 16; i++)
						{
							command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Cộng " + i);
							command_HAIRMOD_PROTECTED.action = delegate
							{
								idPlus = Canvas_HAIRMOD_PROTECTED.menu.selected;
							};
							mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
						}
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
						break;
					}
					}
				}
				else
				{
					if (typeSearch != 1 || select <= -1)
					{
						return;
					}
					ActionPerform_HAIRMOD_PROTECTED ok2 = delegate
					{
						try
						{
							nameChar = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
							Canvas_HAIRMOD_PROTECTED.endDlg();
							if (nameChar.Equals(string.Empty))
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Tên không hợp lệ");
							}
						}
						catch (Exception)
						{
						}
					};
					Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập tên", ok2, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 25, true);
					Canvas_HAIRMOD_PROTECTED.inputDlg.show();
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
				mVector_HAIRMOD_PROTECTED3.addElement(cmdShowText);
				Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 2);
			}
			else
			{
				doView();
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED4.addElement(cmdShowText);
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED4, 2);
		}
		else
		{
			doView();
		}
	}

	public void setItemInventory()
	{
		ALL_ITEM.setElementAt(ItemInInventory_HAIRMOD_PROTECTED.getAllItemCanSell(), 3);
	}

	public bool canBid()
	{
		if (indexMainTile == 0)
		{
			if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0 && !isFocusMainTab && select < ((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size())
			{
				if (select < 0)
				{
					return false;
				}
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
				if (itemInInventory_HAIRMOD_PROTECTED.prizeBet > 0)
				{
					return true;
				}
			}
		}
		else if (indexMainTile == 1 && !isSelectTypeFind && ((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0 && !isFocusMainTab && select < ((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size())
		{
			if (select < 0)
			{
				return false;
			}
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
			if (itemInInventory_HAIRMOD_PROTECTED2.prizeBet > 0)
			{
				return true;
			}
		}
		return false;
	}

	public void setCmdLeft()
	{
		if (indexMainTile == 3)
		{
			setItemInventory();
		}
		if (indexMainTile == 0)
		{
			if (!canBid())
			{
				if (isFocusMainTab)
				{
					left = new Command_HAIRMOD_PROTECTED(string.Empty);
					left.action = delegate
					{
					};
					return;
				}
				if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0)
				{
					left = new Command_HAIRMOD_PROTECTED("Mua");
					left.action = delegate
					{
						if (select >= 0)
						{
							ItemInInventory_HAIRMOD_PROTECTED item4 = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
							doAcceptBuy(0, item4);
						}
					};
				}
			}
			else
			{
				left = new Command_HAIRMOD_PROTECTED("Menu");
				left.action = delegate
				{
					isShowText = false;
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED5 = new mVector_HAIRMOD_PROTECTED();
					mVector_HAIRMOD_PROTECTED5.addElement(new Command_HAIRMOD_PROTECTED("Mua")
					{
						action = delegate
						{
							if (select >= 0)
							{
								doAcceptBuy(0, (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select));
							}
						}
					});
					mVector_HAIRMOD_PROTECTED5.addElement(new Command_HAIRMOD_PROTECTED("Bid")
					{
						action = delegate
						{
							if (select >= 0)
							{
								ItemInInventory_HAIRMOD_PROTECTED item3 = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
								ActionPerform_HAIRMOD_PROTECTED ok3 = delegate
								{
									try
									{
										string text3 = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
										int price3 = int.Parse(text3);
										Canvas_HAIRMOD_PROTECTED.endDlg();
										if (price3 < 0 || price3 <= item3.prizeBet || price3 >= item3.prize)
										{
											Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá bid phải lớn hơn " + item3.prizeBet + " và nhỏ hơn " + item3.prize);
										}
										else
										{
											ActionPerform_HAIRMOD_PROTECTED yesAction4 = delegate
											{
												if (select >= 0)
												{
													GameService_HAIRMOD_PROTECTED.gI().doBid(select, 0, price3);
													Canvas_HAIRMOD_PROTECTED.endDlg();
												}
											};
											Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn đặt bid cho " + item3.getTemplate().name + " với giá " + price3 + " không?", yesAction4);
										}
									}
									catch (Exception)
									{
										Canvas_HAIRMOD_PROTECTED.endDlg();
										Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
									}
								};
								Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập giá", ok3, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 10, true);
								Canvas_HAIRMOD_PROTECTED.inputDlg.show();
							}
						}
					});
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED5, 0);
				};
			}
		}
		else if (indexMainTile == 1)
		{
			left = new Command_HAIRMOD_PROTECTED("Menu");
			left.action = delegate
			{
				isShowText = false;
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
				if (!isFocusMainTab && ((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0 && select > -1)
				{
					if (select < 0)
					{
						return;
					}
					ItemInInventory_HAIRMOD_PROTECTED item2 = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
					if (!item2.isMyItem())
					{
						mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Mua")
						{
							action = delegate
							{
								ActionPerform_HAIRMOD_PROTECTED yesAction3 = delegate
								{
									if (select >= 0)
									{
										doAcceptBuy(1, (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select));
									}
								};
								Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn mua " + item2.getTemplate().name + " với giá " + item2.prize + " không?", yesAction3);
							}
						});
						if (canBid())
						{
							mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Bid")
							{
								action = delegate
								{
									ActionPerform_HAIRMOD_PROTECTED ok2 = delegate
									{
										try
										{
											string text2 = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
											int price2 = int.Parse(text2);
											Canvas_HAIRMOD_PROTECTED.endDlg();
											if (price2 < 0 || price2 <= item2.prizeBet || price2 >= item2.prize)
											{
												Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá bid phải lớn hơn " + item2.prizeBet + " và nhỏ hơn " + item2.prize);
											}
											else
											{
												ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
												{
													if (select >= 0)
													{
														GameService_HAIRMOD_PROTECTED.gI().doBid(select, 1, price2);
														Canvas_HAIRMOD_PROTECTED.endDlg();
													}
												};
												Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn đặt bid cho " + item2.getTemplate().name + " với giá " + price2 + " không?", yesAction2);
											}
										}
										catch (Exception)
										{
											Canvas_HAIRMOD_PROTECTED.endDlg();
											Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
										}
									};
									Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập giá", ok2, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 10, true);
									Canvas_HAIRMOD_PROTECTED.inputDlg.show();
								}
							});
						}
					}
				}
				if (isSelectTypeFind)
				{
					mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Tìm")
					{
						action = delegate
						{
							doFind();
						}
					});
				}
				if (typeSearch != 0)
				{
					mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Tìm theo loại")
					{
						action = delegate
						{
							reset();
							typeSearch = 0;
						}
					});
				}
				if (typeSearch != 1)
				{
					mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Tìm theo người bán")
					{
						action = delegate
						{
							reset();
							typeSearch = 1;
						}
					});
				}
				if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() > 0)
				{
					mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Kết quả")
					{
						action = delegate
						{
							isSelectTypeFind = false;
							select = -1;
							setCmdLeft();
						}
					});
				}
				if (nPage[indexMainTile] > 0)
				{
					if (indexPage[indexMainTile] < nPage[indexMainTile] - 1)
					{
						mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Trang sau")
						{
							action = delegate
							{
								indexPage[indexMainTile]++;
							}
						});
					}
					if (indexPage[indexMainTile] > 0)
					{
						mVector_HAIRMOD_PROTECTED4.addElement(new Command_HAIRMOD_PROTECTED("Trang trước")
						{
							action = delegate
							{
								indexPage[indexMainTile]--;
							}
						});
					}
				}
				Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED4, 0);
			};
		}
		else if (indexMainTile == 2)
		{
			left = cmdDoNothing;
		}
		else if (indexMainTile == 3)
		{
			left = cmdSell;
		}
		else if (indexMainTile == 4)
		{
			mVector_HAIRMOD_PROTECTED v = (mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile);
			if (v.size() > 0 || money > 0)
			{
				left = new Command_HAIRMOD_PROTECTED("Menu");
				left.action = delegate
				{
					isShowText = false;
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
					if (money > 0)
					{
						mVector_HAIRMOD_PROTECTED3.addElement(new Command_HAIRMOD_PROTECTED("Rút tiền")
						{
							action = delegate
							{
								GameService_HAIRMOD_PROTECTED.gI().doGetMoneySellInventory();
							}
						});
					}
					if (v.size() > 0 && select >= 0)
					{
						mVector_HAIRMOD_PROTECTED3.addElement(new Command_HAIRMOD_PROTECTED("Bỏ vào hành trang")
						{
							action = delegate
							{
								GameService_HAIRMOD_PROTECTED.gI().doGetItemBackInventory(select);
							}
						});
					}
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 2);
				};
			}
			else
			{
				left = new Command_HAIRMOD_PROTECTED(string.Empty);
				left.action = delegate
				{
				};
			}
		}
		else if (indexMainTile == 5)
		{
			if (((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).size() == 0 || isFocusMainTab)
			{
				left = new Command_HAIRMOD_PROTECTED(string.Empty);
				left.action = delegate
				{
				};
			}
			else
			{
				left = new Command_HAIRMOD_PROTECTED("Menu");
				left.action = delegate
				{
					isShowText = false;
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
					mVector_HAIRMOD_PROTECTED2.addElement(new Command_HAIRMOD_PROTECTED("Mua")
					{
						action = delegate
						{
							doAcceptBuy(2, (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select));
						}
					});
					mVector_HAIRMOD_PROTECTED2.addElement(new Command_HAIRMOD_PROTECTED("Đặt bid")
					{
						action = delegate
						{
							ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)ALL_ITEM.elementAt(indexMainTile)).elementAt(select);
							ActionPerform_HAIRMOD_PROTECTED ok = delegate
							{
								try
								{
									string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
									int price = int.Parse(text);
									Canvas_HAIRMOD_PROTECTED.endDlg();
									if (price < 0 || price <= item.prizeBet || price >= item.prize)
									{
										Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá bid phải lớn hơn " + item.prizeBet + " và nhỏ hơn " + item.prize);
									}
									else
									{
										ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
										{
											GameService_HAIRMOD_PROTECTED.gI().doBid(select, 2, price);
											Canvas_HAIRMOD_PROTECTED.endDlg();
										};
										Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn đặt bid cho " + item.getTemplate().name + " với giá " + price + " không?", yesAction);
									}
								}
								catch (Exception)
								{
									Canvas_HAIRMOD_PROTECTED.endDlg();
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
								}
							};
							Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập giá", ok, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 10, true);
							Canvas_HAIRMOD_PROTECTED.inputDlg.show();
						}
					});
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
				};
			}
		}
		Debug.LogError("xong---------ee---------------------");
	}

	public static void popupBuyItem(ItemInInventory_HAIRMOD_PROTECTED item)
	{
		TField_HAIRMOD_PROTECTED tfSeri = new TField_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 120, 140, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2);
		tfSeri.setIputType(TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY);
		tfSeri.isFocus = true;
		tfSeri.setText("100000");
		PageScr_HAIRMOD_PROTECTED.gI().right = tfSeri.cmdClear;
		TField_HAIRMOD_PROTECTED tfMaso = new TField_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 60, 140, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2);
		tfMaso.setIputType(TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY);
		tfMaso.setText("10000");
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Bán");
		command_HAIRMOD_PROTECTED.action = delegate
		{
			int num = 0;
			int num2 = 0;
			try
			{
				num = int.Parse(tfSeri.getText().Trim());
				num2 = int.Parse(tfMaso.getText().Trim());
			}
			catch (Exception)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá bán không hợp lệ");
				return;
			}
			if (num < num2)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá bid phải nhỏ hơn giá bán");
			}
			else
			{
				int p = num;
				int pb = num2;
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().doRequestSelItem(0, item.itemID, p, pb);
					PageScr_HAIRMOD_PROTECTED.gI().left.perform();
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi thông tin bán ");
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn đặt bán vật phẩm này với giá " + tfSeri.getText().Trim() + "  không?", yesAction);
			}
		};
		PageScr_HAIRMOD_PROTECTED.gI().setInfo(Canvas_HAIRMOD_PROTECTED.hw - 90, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 170, 180, 140, "NHẬP GIÁ BÁN", command_HAIRMOD_PROTECTED);
		Layer_HAIRMOD_PROTECTED layer_HAIRMOD_PROTECTED = new Layer_HAIRMOD_PROTECTED();
		layer_HAIRMOD_PROTECTED.isUpdate = delegate
		{
			tfSeri.update();
			tfMaso.update();
		};
		layer_HAIRMOD_PROTECTED.isKeyCode = delegate(int keyCode)
		{
			if (tfSeri.isFocus)
			{
				tfSeri.keyPressed(keyCode);
			}
			if (tfMaso.isFocus)
			{
				tfMaso.keyPressed(keyCode);
			}
		};
		layer_HAIRMOD_PROTECTED.isPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
		{
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Giá bán", Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 140, 0);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Giá bid", Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 80, 0);
			tfSeri.paint(g);
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
			tfMaso.paint(g);
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		};
		PageScr_HAIRMOD_PROTECTED.gI().layer = layer_HAIRMOD_PROTECTED;
		PageScr_HAIRMOD_PROTECTED.gI().switchToMe(Canvas_HAIRMOD_PROTECTED.shop);
	}

	public void onItemInfo()
	{
		if (isShowText)
		{
			doView();
		}
	}
}
