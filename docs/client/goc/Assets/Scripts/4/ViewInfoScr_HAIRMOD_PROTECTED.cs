public class ViewInfoScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	private static ViewInfoScr_HAIRMOD_PROTECTED me;

	private mVector_HAIRMOD_PROTECTED text;

	private short w;

	private short h;

	private MyScreen_HAIRMOD_PROTECTED lastScr;

	private int type;

	private short idClan = -1;

	private string title;

	private string status = string.Empty;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	public static int selected;

	public static int lastSe;

	public sbyte nationID = -1;

	private int xa;

	private int dir = 1;

	public ViewInfoScr_HAIRMOD_PROTECTED()
	{
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			lastScr.switchToMe();
			lastScr = null;
			text.removeAllElements();
			me = null;
		};
		command_HAIRMOD_PROTECTED.action = action;
		right = command_HAIRMOD_PROTECTED;
		left = new Command_HAIRMOD_PROTECTED("Menu");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			if (type == 0)
			{
				if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Đặt slogan");
					ActionPerform_HAIRMOD_PROTECTED action3 = delegate
					{
						ActionPerform_HAIRMOD_PROTECTED ok = delegate
						{
							string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
							if (!text.Equals(string.Empty))
							{
								GameService_HAIRMOD_PROTECTED.gI().setSologan(text);
								status = text;
								Canvas_HAIRMOD_PROTECTED.endDlg();
								xa = 0;
								dir = 1;
							}
						};
						Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Slogan", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 50, false);
						Canvas_HAIRMOD_PROTECTED.inputDlg.show();
					};
					command_HAIRMOD_PROTECTED2.action = action3;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Danh sách thành viên");
				ActionPerform_HAIRMOD_PROTECTED action4 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
					GameService_HAIRMOD_PROTECTED.gI().requestClanList(idClan, 0);
				};
				command_HAIRMOD_PROTECTED3.action = action4;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
		};
		left.action = action2;
		w = (short)Canvas_HAIRMOD_PROTECTED.hw;
		h = 170;
		lastSe = (h - 30) / 2 / 14;
	}

	public static ViewInfoScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ViewInfoScr_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		if (lastScr == null)
		{
			lastScr = Canvas_HAIRMOD_PROTECTED.currentScreen;
		}
		base.switchToMe();
	}

	public void setInfo(string title, string str)
	{
		this.title = title;
		type = 2;
		text = new mVector_HAIRMOD_PROTECTED();
		string[] array = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(str, w - 15);
		for (int i = 0; i < array.Length; i++)
		{
			text.addElement(array[i]);
		}
		setLim();
	}

	private void setLim()
	{
		cmyLim = text.size() * 14 - (h - 25) + 20;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		cmy = (cmtoY = 0);
		selected = lastSe;
	}

	public void setInfo(ClanInfo_HAIRMOD_PROTECTED clan)
	{
		title = clan.name.ToUpper();
		type = 0;
		idClan = clan.id;
		text = new mVector_HAIRMOD_PROTECTED();
		status = clan.status;
		text.addElement("Bang chủ: " + clan.master);
		text.addElement("Cấp độ: " + clan.level);
		text.addElement("Xu: " + Canvas_HAIRMOD_PROTECTED.getMoneys(clan.money));
		text.addElement("Thành viên: " + clan.members);
		text.addElement("Điểm cống hiến: " + clan.dedicationPoint);
		text.addElement("Điểm kinh ngiệm: " + clan.xp);
		text.addElement("Thành lập: " + clan.date);
		if (clan.dissolved)
		{
			string[] array = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(clan.strDissolved, w - 15);
			for (int i = 0; i < array.Length; i++)
			{
				text.addElement(array[i]);
			}
		}
		setLim();
	}

	public override void updateKey()
	{
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.keyHold[2])
		{
			selected--;
			if (selected < lastSe)
			{
				selected = lastSe;
			}
			flag = true;
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyHold[8])
		{
			flag = true;
			if (cmy < cmyLim)
			{
				selected++;
			}
			if (selected > text.size() - lastSe + 1)
			{
				selected = text.size() - lastSe + 1;
			}
		}
		if (flag)
		{
			cmtoY = selected * 14 - (h - 25) / 2;
			if (cmtoY < 0)
			{
				cmtoY = 0;
			}
			if (cmtoY > cmyLim)
			{
				cmtoY = cmyLim;
			}
		}
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
		base.updateKey();
		if (lastScr != null)
		{
			lastScr.update();
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (lastScr != null)
		{
			lastScr.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.translate(Canvas_HAIRMOD_PROTECTED.hw - w / 2, Canvas_HAIRMOD_PROTECTED.hh - h / 2);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 0, 0, w, h);
		g.setClip(-w / 2, 0, 2 * w, h);
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, title, w >> 1, 4, 2);
		g.setClip(0, 27, w, h - 30);
		g.translate(0, -cmy);
		if (type == 2)
		{
			paintText(g);
		}
		else
		{
			paintTextNormal(g);
		}
		base.paint(g);
	}

	private void paintTextNormal(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 34;
		if (nationID > -1)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNation, 0, nationID * 11, 11, 11, 0, 5, num, 20);
			num += 15;
		}
		for (int i = 0; i < text.size(); i++)
		{
			string st = (string)text.elementAt(i);
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, st, 5, num, 0);
			num += 14;
		}
	}

	private void paintText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 34;
		if (idClan != -1)
		{
			GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(idClan + 1000), w / 2, num);
			num += 6;
		}
		if (type == 0 && !status.Equals(string.Empty))
		{
			int width = MFont_HAIRMOD_PROTECTED.arialFontW.getWidth(status);
			if (width > w)
			{
				if (dir == 1)
				{
					xa--;
				}
				else
				{
					xa++;
				}
				if (Math_HAIRMOD_PROTECTED.abs(xa) > width / 2 - w / 2 + 10)
				{
					dir *= -1;
				}
			}
			g.setClip(3, 0, w - 6, h);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, status, (w >> 1) + xa, num, 2);
			num += 14;
		}
		for (int i = 0; i < text.size(); i++)
		{
			string st = (string)text.elementAt(i);
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, st, 5, num, 0);
			num += 14;
		}
	}
}
