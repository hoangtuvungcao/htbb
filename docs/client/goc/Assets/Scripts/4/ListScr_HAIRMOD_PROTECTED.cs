using System;

public class ListScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static ListScr_HAIRMOD_PROTECTED me;

	public static sbyte FRIEND;

	public static sbyte CLAN = 1;

	public static sbyte MSG_CLAN = 2;

	public static sbyte STRONGER = 4;

	public static sbyte RIGHER = 3;

	public static sbyte TOP_CLAN = 5;

	public static sbyte MANAGER_LIST = 6;

	public static sbyte TOP = 7;

	public static sbyte TOP_CUC_BO = 8;

	public mVector_HAIRMOD_PROTECTED freindList;

	public mVector_HAIRMOD_PROTECTED list;

	private int w;

	private int h;

	private int height;

	private int type;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	public static int selected;

	public static int xTr;

	public static int yTr;

	private string title;

	public sbyte page;

	private MyScreen_HAIRMOD_PROTECTED lastScr;

	private Command_HAIRMOD_PROTECTED cmd;

	private int pa;

	private bool trans;

	private int x0;

	private int dir0 = 1;

	private int x1;

	private int dir1 = 1;

	private int x2;

	private int dir2 = 1;

	private string[] nameManager;

	public bool transY;

	public bool canSelecet;

	public bool isTabIp;

	public bool isPaintFc;

	private long timePointY;

	private long count;

	private long timeOpen;

	private long timeDelay;

	private int pyLast;

	private int dyTran;

	private int dxTran;

	private int xBegin;

	private int yBegin;

	private int wTouch;

	private int hTouch;

	private int curentTabUpdate;

	private int timeHold;

	public float vY;

	public ListScr_HAIRMOD_PROTECTED()
	{
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			lastScr.switchToMe();
			for (int i = 0; i < list.size(); i++)
			{
				object obj = list.elementAt(i);
				if (obj != null)
				{
					obj = null;
				}
			}
			list.removeAllElements();
			GC.Collect();
			lastScr = null;
		};
		right.action = action;
		cmd = new Command_HAIRMOD_PROTECTED();
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			doSelected(list.elementAt(selected));
		};
		cmd.action = action2;
		left = new Command_HAIRMOD_PROTECTED("Menu");
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			if (selected > -1)
			{
				doMenu();
			}
		};
		left.action = action3;
	}

	public static ListScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ListScr_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen != this)
		{
			selected = -1;
			page = 0;
			cmy = (cmtoY = 0);
			if (lastScr == null)
			{
				lastScr = Canvas_HAIRMOD_PROTECTED.gameScr;
			}
			base.switchToMe();
			init();
		}
	}

	public override void init()
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
		w = num - 41;
		h = num2 - 56;
		if (Canvas_HAIRMOD_PROTECTED.w * Canvas_HAIRMOD_PROTECTED.h > 153600)
		{
			xTr = (Canvas_HAIRMOD_PROTECTED.w - num) / 2;
			yTr = (Canvas_HAIRMOD_PROTECTED.h - num2) / 2;
		}
		cmyLim = list.size() * height - (h - 42);
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
	}

	protected void doMenu()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if (type == STRONGER || type == RIGHER || type == TOP_CLAN || type == CLAN)
		{
			if (type == TOP_CLAN)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Danh sách quản lý bang.");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().requestQuanLyBang(((ClanInfo_HAIRMOD_PROTECTED)list.elementAt(selected)).id);
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Xem thêm");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.startWaitDlg();
				if (type == CLAN)
				{
					GameService_HAIRMOD_PROTECTED.gI().requestClanList(((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).idClan, ++page);
				}
				else
				{
					GameService_HAIRMOD_PROTECTED.gI().requestTopStronger_Righer(type, ++page);
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		}
		if (type == FRIEND)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Xóa bạn");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				if (freindList.size() == 0)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Danh sách trống");
				}
				else
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().removeFriend(((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).name);
						list.removeElementAt(selected);
						freindList.removeElementAt(selected);
						selected--;
						if (selected < 0)
						{
							selected = 0;
						}
						setPos();
						cmyLim = list.size() * height - (h - 32);
						if (cmyLim < 0)
						{
							cmyLim = 0;
						}
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã xóa bạn");
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn xóa người này không ?", yesAction2);
				}
			};
			command_HAIRMOD_PROTECTED3.action = action3;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
		}
		if (type == CLAN)
		{
			if (((Char_HAIRMOD_PROTECTED)list.elementAt(0)).idClan == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Nhắn tin toàn bang");
				ActionPerform_HAIRMOD_PROTECTED action4 = delegate
				{
					ActionPerform_HAIRMOD_PROTECTED ok = delegate
					{
						string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
						if (!text.Equals(string.Empty))
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi tin");
							GameService_HAIRMOD_PROTECTED.gI().msgClanAll(text, 0, 0);
						}
					};
					Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nội dung", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 100, false);
					Canvas_HAIRMOD_PROTECTED.inputDlg.show();
				};
				command_HAIRMOD_PROTECTED4.action = action4;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED4);
			}
			if (((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).idClan == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan && (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0 || Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 1) && !((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).name.Equals(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name))
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Mời khỏi bang");
				ActionPerform_HAIRMOD_PROTECTED action5 = delegate
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().requestEvictionClan(((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).name);
						list.removeElementAt(selected);
						selected--;
						Canvas_HAIRMOD_PROTECTED.endDlg();
						setPos();
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có chắc muốn mời " + ((Char_HAIRMOD_PROTECTED)list.elementAt(selected)).name + " khỏi bang không ?", yesAction);
				};
				command_HAIRMOD_PROTECTED5.action = action5;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED5);
			}
		}
		else if (type == MSG_CLAN && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Xóa tin nhắn");
			ActionPerform_HAIRMOD_PROTECTED action6 = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().msgClanAll(null, 2, ((ChatInfo_HAIRMOD_PROTECTED)list.elementAt(selected)).ID);
				list.removeElementAt(selected);
				selected--;
				if (selected < 0)
				{
					selected = 0;
				}
			};
			command_HAIRMOD_PROTECTED6.action = action6;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED6);
		}
		if (mVector_HAIRMOD_PROTECTED2.size() > 0)
		{
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
		}
	}

	public void doSelected(object ch)
	{
		if (type == MSG_CLAN)
		{
			ViewTextDlg_HAIRMOD_PROTECTED.gI().setInfo(((ChatInfo_HAIRMOD_PROTECTED)ch).content, "TIN NHẮN");
			return;
		}
		if (type == TOP_CLAN)
		{
			GameService_HAIRMOD_PROTECTED.gI().requestClanInfo(((ClanInfo_HAIRMOD_PROTECTED)ch).id);
			return;
		}
		ActionPerform_HAIRMOD_PROTECTED ok = delegate
		{
			GameService_HAIRMOD_PROTECTED.gI().sendMsgPrivate(((Char_HAIRMOD_PROTECTED)ch).name, Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nội dung tin nhắn", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 100, false);
		Canvas_HAIRMOD_PROTECTED.inputDlg.show();
	}

	public void setInfo(mVector_HAIRMOD_PROTECTED list, int type, string title)
	{
		selected = -1;
		if (list.size() == 0)
		{
			page = -1;
		}
		else
		{
			this.list = list;
		}
		this.type = type;
		if (type == MSG_CLAN)
		{
			height = 30;
		}
		else
		{
			height = 40;
		}
		this.title = title;
		setPos();
		if (this.list != null)
		{
			cmyLim = this.list.size() * height - (h - 32);
		}
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		if (type == MANAGER_LIST && nameManager == null)
		{
			nameManager = new string[3] { "Bang chủ", "Phó bang", "Trưởng lão" };
		}
	}

	private void setPos()
	{
		if (type != MSG_CLAN && type != TOP_CLAN)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)list.elementAt(i);
				char_HAIRMOD_PROTECTED.x = 25;
				char_HAIRMOD_PROTECTED.y = (short)(i * height + height / 2 + char_HAIRMOD_PROTECTED.height / 2 - 5);
			}
		}
	}

	public override void update()
	{
		updateKeyDong();
		if (lastScr != null)
		{
			lastScr.update();
		}
	}

	public override void updateKey()
	{
		xBegin = 20 + xTr;
		yBegin = 46 + yTr;
		wTouch = w;
		hTouch = h - 24;
		base.updateKey();
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (lastScr != null)
		{
			lastScr.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 20 + xTr, 10 + yTr, w, h);
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, title, 20 + w / 2 + xTr, 15 + yTr, 2);
		g.translate(20, 37);
		g.setClip(4 + xTr, yTr, w - 8, h - 41);
		g.translate(xTr, -cmy + yTr);
		if (selected > -1)
		{
			Res_HAIRMOD_PROTECTED.paintSelected(g, 4, selected * height, w - 9, height);
		}
		if (type == TOP_CLAN)
		{
			paintTopClan(g);
		}
		else if (type == MSG_CLAN)
		{
			paintMsgClan(g);
		}
		else
		{
			paintChar(g);
		}
		base.paint(g);
	}

	private void paintTopClan(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = cmy / height;
		if (num <= 0)
		{
			num = 0;
		}
		int num2 = num + h / height + 1;
		if (num2 >= list.size())
		{
			num2 = list.size();
		}
		for (int i = num; i < num2; i++)
		{
			ClanInfo_HAIRMOD_PROTECTED clanInfo_HAIRMOD_PROTECTED = (ClanInfo_HAIRMOD_PROTECTED)list.elementAt(i);
			if (clanInfo_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
			int num3 = 0;
			if (clanInfo_HAIRMOD_PROTECTED.id != -1 && GameData_HAIRMOD_PROTECTED.getImgIcon(clanInfo_HAIRMOD_PROTECTED.id) != null)
			{
				num3 = 12;
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(clanInfo_HAIRMOD_PROTECTED.id + 1000), 16, i * height + 13);
			}
			string empty = string.Empty;
			empty = clanInfo_HAIRMOD_PROTECTED.name + " - Cấp độ: " + ((clanInfo_HAIRMOD_PROTECTED.level <= -1) ? "?" : (clanInfo_HAIRMOD_PROTECTED.level + string.Empty));
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, empty, 11 + num3, i * height + 4, 0);
			int width = MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(empty);
			if (clanInfo_HAIRMOD_PROTECTED.nationID > -1)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNation, 0, clanInfo_HAIRMOD_PROTECTED.nationID * 11, 11, 11, 0, 11 + num3 + 25 + width, i * height + 4, 0);
			}
			string text = "Bang chủ: " + clanInfo_HAIRMOD_PROTECTED.master + " - Tiền: " + clanInfo_HAIRMOD_PROTECTED.money + "xu - Thành viên: " + clanInfo_HAIRMOD_PROTECTED.members;
			if (i == selected)
			{
				int width2 = MFont_HAIRMOD_PROTECTED.arialFontW.getWidth(text);
				if (width2 > w - 10 && selected == i)
				{
					if (dir1 == 1)
					{
						x1--;
						if (x1 < w - 20 - width2)
						{
							dir1 = -1;
						}
					}
					else
					{
						x1++;
						if (x1 > 5)
						{
							dir1 = 1;
						}
					}
				}
			}
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, text, 11 + ((i == selected) ? x1 : 0), i * height + 20, 0);
		}
	}

	private void paintMsgClan(MyGraphics_HAIRMOD_PROTECTED g)
	{
		string empty = string.Empty;
		int num = cmy / height;
		if (num <= 0)
		{
			num = 0;
		}
		int num2 = num + h / height + 1;
		if (num2 >= list.size())
		{
			num2 = list.size();
		}
		for (int num3 = num2 - 1; num3 >= num; num3--)
		{
			ChatInfo_HAIRMOD_PROTECTED chatInfo_HAIRMOD_PROTECTED = (ChatInfo_HAIRMOD_PROTECTED)list.elementAt(num3);
			if (chatInfo_HAIRMOD_PROTECTED != null)
			{
				MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, chatInfo_HAIRMOD_PROTECTED.name, 7, num3 * height, 0);
				empty = ((MFont_HAIRMOD_PROTECTED.arialFont.getWidth(chatInfo_HAIRMOD_PROTECTED.content) <= w - 10) ? chatInfo_HAIRMOD_PROTECTED.content : (MFont_HAIRMOD_PROTECTED.arialFont.splitFontBStrInLine(chatInfo_HAIRMOD_PROTECTED.content, w - 10)[0] + "..."));
				MFont_HAIRMOD_PROTECTED.arialFont.drawString(g, empty, 7, num3 * height + 15, 0);
			}
		}
	}

	public void getInfoCharString()
	{
		int num = cmy / height;
		int num2 = num + h / height + 1;
		if (num2 >= list.size())
		{
			num2 = list.size();
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = null;
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = null;
		for (int i = num; i < num2; i++)
		{
			char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)list.elementAt(i);
			string text = "Cấp độ: " + char_HAIRMOD_PROTECTED.level;
			int num3 = char_HAIRMOD_PROTECTED.wearingItems.size();
			for (int j = 0; j < num3; j++)
			{
				itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)char_HAIRMOD_PROTECTED.wearingItems.elementAt(j);
				if (itemInInventory_HAIRMOD_PROTECTED != null)
				{
					itemTemplate_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
					if (itemTemplate_HAIRMOD_PROTECTED != null)
					{
					}
					string text2 = text;
					text = text2 + " - " + ItemTemplate_HAIRMOD_PROTECTED.nameItem[itemTemplate_HAIRMOD_PROTECTED.type] + ": " + itemInInventory_HAIRMOD_PROTECTED.level;
					if (itemInInventory_HAIRMOD_PROTECTED.plusTemplate > 0)
					{
						text = text + "+" + itemInInventory_HAIRMOD_PROTECTED.plusTemplate;
					}
				}
			}
		}
	}

	private void paintChar(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = cmy / height;
		if (num <= 0)
		{
			num = 0;
		}
		int num2 = num + h / height + 1;
		if (num2 >= list.size())
		{
			num2 = list.size();
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = null;
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = null;
		for (int i = num; i < num2; i++)
		{
			char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)list.elementAt(i);
			if (char_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
			char_HAIRMOD_PROTECTED.paintAvatar(g);
			string text = "Cấp độ: " + ((char_HAIRMOD_PROTECTED.level <= -1) ? "?" : (char_HAIRMOD_PROTECTED.level + string.Empty));
			int num3 = char_HAIRMOD_PROTECTED.wearingItems.size();
			for (int j = 0; j < num3; j++)
			{
				itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)char_HAIRMOD_PROTECTED.wearingItems.elementAt(j);
				if (itemInInventory_HAIRMOD_PROTECTED != null)
				{
					itemTemplate_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
					if (itemTemplate_HAIRMOD_PROTECTED != null)
					{
					}
					string text2 = text;
					text = text2 + " - " + ItemTemplate_HAIRMOD_PROTECTED.nameItem[itemTemplate_HAIRMOD_PROTECTED.type] + ": " + itemInInventory_HAIRMOD_PROTECTED.level;
					if (itemInInventory_HAIRMOD_PROTECTED.plusTemplate > 0)
					{
						text = text + "+" + itemInInventory_HAIRMOD_PROTECTED.plusTemplate;
					}
				}
			}
			int num4 = 0;
			if (char_HAIRMOD_PROTECTED.idClan != -1 && GameData_HAIRMOD_PROTECTED.getImgIcon(char_HAIRMOD_PROTECTED.idClan) != null)
			{
				num4 = 8;
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(char_HAIRMOD_PROTECTED.idClan + 1000), 45, i * height + 12);
			}
			string text3 = Canvas_HAIRMOD_PROTECTED.getPriceMoney(char_HAIRMOD_PROTECTED.charXu, char_HAIRMOD_PROTECTED.luong);
			string text4 = " - CT: " + char_HAIRMOD_PROTECTED.congTrang + " - LT: " + char_HAIRMOD_PROTECTED.lienTram;
			if (!text3.Equals(string.Empty))
			{
				text3 = " (" + text3 + ")";
			}
			int num5 = MFont_HAIRMOD_PROTECTED.arialFontW.getWidth(char_HAIRMOD_PROTECTED.name + text3 + text4) + 30;
			if (num5 > w - 55 && selected == i)
			{
				if (dir2 == 1)
				{
					x2--;
					if (x2 < w - 65 - num5)
					{
						dir2 = -1;
					}
				}
				else
				{
					x2++;
					if (x2 > 5)
					{
						dir2 = 1;
					}
				}
			}
			g.setClip(45, cmy, w - 49, h - 41);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, char_HAIRMOD_PROTECTED.name + text3 + ((type != MANAGER_LIST || char_HAIRMOD_PROTECTED.isMaster >= 3) ? string.Empty : (" - " + nameManager[char_HAIRMOD_PROTECTED.isMaster])) + text4, 45 + num4 + ((i == selected) ? x2 : 0), i * height + 5, 0);
			int num6 = 0;
			num6 = MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(char_HAIRMOD_PROTECTED.name + text3 + ((type != MANAGER_LIST || char_HAIRMOD_PROTECTED.isMaster >= 3) ? string.Empty : (" - " + nameManager[char_HAIRMOD_PROTECTED.isMaster])) + text4);
			if (char_HAIRMOD_PROTECTED.nationID > -1)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNation, 0, char_HAIRMOD_PROTECTED.nationID * 11, 11, 11, 0, 45 + num4 + ((i == selected) ? x2 : 0) + num6 + 5, i * height + 9, 0);
			}
			int width = MFont_HAIRMOD_PROTECTED.arialFontW.getWidth(text);
			if (width > w - 45 && selected == i)
			{
				if (dir0 == 1)
				{
					x0--;
					if (x0 < w - 55 - width)
					{
						dir0 = -1;
					}
				}
				else
				{
					x0++;
					if (x0 > 5)
					{
						dir0 = 1;
					}
				}
			}
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, text, 45 + ((i == selected) ? x0 : 0), i * height + 20, 0);
			g.setClip(0, cmy, w - 4, h - 31);
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
			if (selected > -1)
			{
				if (cmd != null)
				{
					cmd.perform();
				}
				else if (left != null)
				{
					left.perform();
				}
			}
			x0 = 0;
			canSelecet = false;
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownItem && Canvas_HAIRMOD_PROTECTED.isPointer(xBegin, yBegin, wTouch, hTouch))
		{
			pyLast = Canvas_HAIRMOD_PROTECTED.pyLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownItem = false;
			timeDelay = count;
			pa = cmy;
			transY = true;
			vY = 0f;
		}
		if (transY)
		{
			long num = count - timeDelay;
			int num2 = pyLast - Canvas_HAIRMOD_PROTECTED.py;
			pyLast = Canvas_HAIRMOD_PROTECTED.py;
			if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
			{
				if (count % 2 == 0)
				{
					dyTran = Canvas_HAIRMOD_PROTECTED.py;
					dxTran = Canvas_HAIRMOD_PROTECTED.px;
					timePointY = count;
				}
				vY = 0f;
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
				cmy = cmtoY;
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
					isPaintFc = false;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerRelease)
			{
				transY = false;
				int num3 = (int)(count - timePointY);
				float num4 = dyTran - Canvas_HAIRMOD_PROTECTED.py;
				float num5 = dxTran - Canvas_HAIRMOD_PROTECTED.px;
				if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
				{
					vY = num4 / (float)num3 * 10f;
				}
				timePointY = -1L;
				if (Main_HAIRMOD_PROTECTED.canvas.canPutKey() && timeHold != 5)
				{
					if (num <= 4)
					{
						timeHold = 0;
						timeOpen = 5L;
						canSelecet = true;
						checkSelect();
					}
				}
				else if (timeHold >= 5)
				{
					timeHold = 0;
					timeOpen = 5L;
					canSelecet = true;
					checkSelect();
				}
			}
		}
		moveCamera();
	}

	public void checkSelect()
	{
		int num = 36;
		int num2 = (cmtoY + Canvas_HAIRMOD_PROTECTED.py - num - yTr) / height;
		selected = num2;
		if (selected < 0)
		{
			selected = 0;
		}
		if (selected > list.size() - 1)
		{
			selected = list.size() - 1;
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
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
	}
}
