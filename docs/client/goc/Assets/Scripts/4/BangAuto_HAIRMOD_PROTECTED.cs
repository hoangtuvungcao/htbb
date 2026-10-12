using System;

public class BangAuto_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static int bomHp = 50;

	public static int bomMp = 50;

	public static int tgDelay = 1;

	public int seleted;

	public int index;

	public int focus;

	private bool isChangeTab;

	private int countL;

	private int countR;

	public static string[] name = new string[3] { "Trị", "Đánh", "Nhặt" };

	public TField_HAIRMOD_PROTECTED tfHp;

	public TField_HAIRMOD_PROTECTED tfMp;

	public TField_HAIRMOD_PROTECTED tfDelay;

	public bool isAutoParty;

	public bool isAutoPotion;

	public bool isAutoItem;

	public bool isAutoGemItem;

	public bool isNhatHet;

	public bool isOptionFocus;

	public bool isAutoFire;

	public bool isAutoMHP;

	public bool isAll;

	private Command_HAIRMOD_PROTECTED cmdCheck;

	private Command_HAIRMOD_PROTECTED cmdBatTat;

	private Command_HAIRMOD_PROTECTED cmdSelectSkill;

	public static int cmtoX;

	public static int cmx;

	public static int cmdx;

	public static int cmvx;

	public static int cmxLim;

	public static bool isAutoComeHome;

	private Command_HAIRMOD_PROTECTED cmdComeHome;

	private int wT;

	private int hT;

	public bool trans;

	public int pa;

	private string[] optionFC = new string[5] { "Chế độ thường", "Đánh quái ", "Chọn NPC", "Chế độ chiếm thành", "Chế độ quốc chiến" };

	private string[] optionDetail = new string[5] { "Chọn toàn bộ", "Chỉ chọn quái, người trong chế độ đồ sát và khác quốc gia ", "Chỉ chọn NPC", "Chỉ chọn người khác bang", "Chỉ người khác nước" };

	private string[] info;

	public sbyte currentIndex;

	private int xC;

	private int yC;

	private Scroll_HAIRMOD_PROTECTED mScroll = new Scroll_HAIRMOD_PROTECTED();

	private int[] IDSkillBuff = new int[3] { -1, -1, -1 };

	private int[] IDSkillAuto = new int[3] { -1, -1, -1 };

	private int[] vitri = new int[3]
	{
		Canvas_HAIRMOD_PROTECTED.hw - 110,
		Canvas_HAIRMOD_PROTECTED.hw - 10,
		Canvas_HAIRMOD_PROTECTED.hw + 90
	};

	private int xxx;

	public BangAuto_HAIRMOD_PROTECTED()
	{
		tfHp = new TField_HAIRMOD_PROTECTED();
		tfHp.width = 30;
		tfHp.height = 20;
		tfHp.isFocus = false;
		tfHp.setIputType(TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC);
		tfHp.setText(bomHp + string.Empty);
		tfMp = new TField_HAIRMOD_PROTECTED();
		tfMp.width = 30;
		tfMp.height = 20;
		tfMp.isFocus = false;
		tfMp.setIputType(TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC);
		tfMp.setText(string.Empty + bomMp);
		tfDelay = new TField_HAIRMOD_PROTECTED();
		tfDelay.width = 30;
		tfDelay.height = 20;
		tfDelay.isFocus = false;
		tfDelay.setIputType(TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC);
		tfDelay.setText(string.Empty + tgDelay);
		cmdBatTat = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdBatTat.action = delegate
		{
			if (Tilemap_HAIRMOD_PROTECTED.lv == 0 || Tilemap_HAIRMOD_PROTECTED.lv == 201 || Tilemap_HAIRMOD_PROTECTED.lv == 70 || Tilemap_HAIRMOD_PROTECTED.lv == 80)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể bật auto ở đây.");
			}
			else
			{
				doAuto();
			}
		};
		center = cmdBatTat;
		cmdCheck = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdCheck.action = delegate
		{
			doCheck();
		};
		cmdSelectSkill = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdSelectSkill.action = delegate
		{
			doSelectedSkillAuto();
		};
		cmdComeHome = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdComeHome.action = delegate
		{
			isAutoComeHome = !isAutoComeHome;
		};
		left = new Command_HAIRMOD_PROTECTED("Đóng");
		left.action = delegate
		{
			mScroll.clear();
			dosetNumbeMpHp();
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		right = null;
	}

	public override void switchToMe()
	{
		init();
		center.caption = "Chọn";
		center = cmdBatTat;
		isOptionFocus = false;
		base.switchToMe();
	}

	public override void init()
	{
		wT = Canvas_HAIRMOD_PROTECTED.w;
		if (wT > 480)
		{
			wT = 480;
		}
		hT = Canvas_HAIRMOD_PROTECTED.h;
		if (hT > 320)
		{
			hT = 320;
		}
		tfMp.x = (tfHp.x = Canvas_HAIRMOD_PROTECTED.hw + 30);
		tfHp.y = Canvas_HAIRMOD_PROTECTED.hh - 55;
		tfMp.y = Canvas_HAIRMOD_PROTECTED.hh - 30;
		seleted = 0;
		index = 0;
		tfHp.isFocus = false;
		tfMp.isFocus = false;
		isChangeTab = true;
		xC = tfHp.x + 5;
		yC = Canvas_HAIRMOD_PROTECTED.hh + 18;
		base.init();
	}

	public void dosetNumbeMpHp()
	{
		try
		{
			bomHp = int.Parse(tfHp.getText());
			bomMp = int.Parse(tfMp.getText());
		}
		catch (Exception)
		{
		}
	}

	public void doAuto()
	{
		try
		{
			if (tfHp.getText().Equals(string.Empty) || tfMp.getText().Equals(string.Empty))
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("HP, Mp không được bỏ trống.");
				return;
			}
			if (int.Parse(tfHp.getText()) > 100 || int.Parse(tfMp.getText()) > 100)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Giá trị không hợp lệ.");
				return;
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			string[] array = new string[3] { "Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu" };
			if (GameScr_HAIRMOD_PROTECTED.autoFight || GameScr_HAIRMOD_PROTECTED.autoBomHMP)
			{
				array = new string[4] { "Auto đánh", "Auto bơm máu", "Auto đánh, bơm máu", "Tắt auto" };
			}
			for (int i = 0; i < array.Length; i++)
			{
				int o = i;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(array[i]);
				command_HAIRMOD_PROTECTED.action = delegate
				{
					setOptionAuto(o);
				};
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
		}
		catch (Exception)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Vui lòng chỉ nhập số.");
		}
	}

	public void setOptionAuto(int i)
	{
		isAutoFire = false;
		isAll = false;
		isAutoMHP = false;
		switch (i)
		{
		case 0:
			isAutoFire = true;
			GameScr_HAIRMOD_PROTECTED.autoFight = true;
			GameScr_HAIRMOD_PROTECTED.autoBomHMP = false;
			break;
		case 1:
			isAutoMHP = true;
			GameScr_HAIRMOD_PROTECTED.autoFight = false;
			GameScr_HAIRMOD_PROTECTED.autoBomHMP = true;
			break;
		case 2:
			isAll = true;
			GameScr_HAIRMOD_PROTECTED.autoFight = true;
			GameScr_HAIRMOD_PROTECTED.autoBomHMP = true;
			break;
		default:
			GameScr_HAIRMOD_PROTECTED.autoFight = false;
			GameScr_HAIRMOD_PROTECTED.autoBomHMP = false;
			break;
		}
		GameScr_HAIRMOD_PROTECTED.saveAutoFight = GameScr_HAIRMOD_PROTECTED.autoFight;
		bomHp = int.Parse(tfHp.getText());
		bomMp = int.Parse(tfMp.getText());
		tgDelay = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 100) + 10;
		GameScr_HAIRMOD_PROTECTED.timeDelayHpMp = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000) + 1;
		Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		if (GameScr_HAIRMOD_PROTECTED.autoFight)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.lastXAuto = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.x;
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.lastYAuto = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.y;
		}
	}

	private void doCheck()
	{
		if (index == 0)
		{
			isAutoParty = !isAutoParty;
		}
		else
		{
			if (index != 2)
			{
				return;
			}
			switch (seleted)
			{
			case 1:
				isAutoPotion = !isAutoPotion;
				break;
			case 2:
				isAutoItem = !isAutoItem;
				break;
			case 3:
				isAutoGemItem = !isAutoGemItem;
				break;
			case 4:
				isNhatHet = !isNhatHet;
				if (isNhatHet)
				{
					isAutoGemItem = (isAutoItem = (isAutoPotion = true));
				}
				else
				{
					isAutoGemItem = (isAutoItem = (isAutoPotion = false));
				}
				break;
			}
			if (isAutoPotion && isAutoItem && isAutoGemItem)
			{
				isNhatHet = true;
			}
			else
			{
				isNhatHet = false;
			}
		}
	}

	private void setLim()
	{
		cmxLim = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.getnSkill() * 23 - 114;
		if (cmxLim < 0)
		{
			cmxLim = 0;
		}
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.update();
		if (!isOptionFocus)
		{
			if (cmx != cmtoX)
			{
				cmvx = cmtoX - cmx << 2;
				cmdx += cmvx;
				cmx += cmdx >> 4;
				cmdx &= 15;
			}
			if (Math_HAIRMOD_PROTECTED.abs(cmtoX - cmx) < 15 && cmx < 0)
			{
				cmtoX = 0;
			}
			if (Math_HAIRMOD_PROTECTED.abs(cmtoX - cmx) < 10 && cmx > cmxLim)
			{
				cmtoX = cmxLim;
			}
			tfHp.update();
			tfMp.update();
		}
		base.update();
	}

	public override void updateKey()
	{
		if (isOptionFocus)
		{
			bool flag = false;
			bool flag2 = false;
			if (Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.w / 2 - 75, Canvas_HAIRMOD_PROTECTED.h / 2 - 80, 75, 30))
			{
				flag = true;
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.w / 2, Canvas_HAIRMOD_PROTECTED.h / 2 - 80, 75, 30))
			{
				flag2 = true;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				if (flag)
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[4] = true;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				else if (flag2)
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[6] = true;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				GameScr_HAIRMOD_PROTECTED.typeOptionFocus--;
				if (GameScr_HAIRMOD_PROTECTED.typeOptionFocus < 0)
				{
					GameScr_HAIRMOD_PROTECTED.typeOptionFocus = (sbyte)(optionFC.Length - 1);
				}
				setInfo();
				countL = 5;
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				GameScr_HAIRMOD_PROTECTED.typeOptionFocus++;
				if (GameScr_HAIRMOD_PROTECTED.typeOptionFocus > optionFC.Length - 1)
				{
					GameScr_HAIRMOD_PROTECTED.typeOptionFocus = 0;
				}
				setInfo();
				countR = 5;
			}
		}
		else
		{
			ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = mScroll.updateKey();
			focus = scrollResult_HAIRMOD_PROTECTED.selected;
			mScroll.updatecm();
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.hw - 150, Canvas_HAIRMOD_PROTECTED.hh - 120, 300, 23))
			{
				mScroll.clear();
				index = (Canvas_HAIRMOD_PROTECTED.px - (Canvas_HAIRMOD_PROTECTED.hw - 150)) / 100;
				if (index > 1)
				{
					index = 1;
				}
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			}
			switch (index)
			{
			case 0:
				if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer(xC, yC, 20, 20))
				{
					seleted = 3;
					isAutoParty = !isAutoParty;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				else if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer(xC, yC + 25, 20, 20))
				{
					seleted = 4;
					isAutoComeHome = !isAutoComeHome;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				break;
			case 1:
				if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.hw - 150, Canvas_HAIRMOD_PROTECTED.hh + 53, 300, 30))
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					Out_HAIRMOD_PROTECTED.println("click   " + focus);
					if (focus != -1)
					{
						doSelectedSkillAuto();
					}
				}
				break;
			case 2:
				if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.hw + 20, Canvas_HAIRMOD_PROTECTED.hh - 45, 40, 100))
				{
					seleted = (Canvas_HAIRMOD_PROTECTED.py - (Canvas_HAIRMOD_PROTECTED.hh - 40)) / 25 + 1;
					if (seleted > 4)
					{
						seleted = 4;
					}
					doCheck();
				}
				break;
			}
		}
		base.updateKey();
	}

	public override bool keyPress(int keyCode)
	{
		if (tfHp.isFocus)
		{
			if (tfHp.keyPressed(keyCode))
			{
				return true;
			}
		}
		else if (tfMp.isFocus)
		{
			if (tfMp.keyPressed(keyCode))
			{
				return true;
			}
		}
		else if (tfDelay.isFocus && tfDelay.keyPressed(keyCode))
		{
			return true;
		}
		return base.keyPress(keyCode);
	}

	public void setInfo()
	{
		info = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(optionDetail[GameScr_HAIRMOD_PROTECTED.typeOptionFocus], 135);
		center = new Command_HAIRMOD_PROTECTED("Chọn");
		center.action = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		right.action = delegate
		{
			GameScr_HAIRMOD_PROTECTED.typeOptionFocus = currentIndex;
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		left = new Command_HAIRMOD_PROTECTED(string.Empty);
		left.action = delegate
		{
		};
	}

	public void paintTabAuto(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = Canvas_HAIRMOD_PROTECTED.hw - 150;
		int num2 = Canvas_HAIRMOD_PROTECTED.hh - 120;
		int w = 300;
		int h = 210;
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, num, num2, w, h);
		int[] array = new int[3]
		{
			Canvas_HAIRMOD_PROTECTED.hw - 100,
			Canvas_HAIRMOD_PROTECTED.hw,
			Canvas_HAIRMOD_PROTECTED.hw + 100
		};
		g.setColor(9755120, 0.5f);
		int num3 = ((index != 1) ? 3 : 0);
		g.fillRect(index * 100 + num + num3, num2 + 3, 100 - num3, 22);
		for (int i = 0; i < name.Length; i++)
		{
			if (i == index)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, name[i], array[i], num2 + 5, MFont_HAIRMOD_PROTECTED.CENTER);
			}
			else
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, name[i], array[i], num2 + 5, MFont_HAIRMOD_PROTECTED.CENTER);
			}
			g.setColor(10591392);
			if (i > 0)
			{
				g.fillRect(i * 100 + num, num2 + 3, 2, 22);
			}
		}
		switch (index)
		{
		case 0:
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "MP : ", Canvas_HAIRMOD_PROTECTED.hw - 60, tfMp.y, MFont_HAIRMOD_PROTECTED.LEFT);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "HP : ", Canvas_HAIRMOD_PROTECTED.hw - 60, tfHp.y, MFont_HAIRMOD_PROTECTED.LEFT);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "%", tfHp.x + 55, tfHp.y, MFont_HAIRMOD_PROTECTED.RIGHT);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "%", tfMp.x + 55, tfMp.y + 2, MFont_HAIRMOD_PROTECTED.RIGHT);
			g.setColor(16777215);
			g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 65, Canvas_HAIRMOD_PROTECTED.hh + 5, 135, 1);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Tự vào nhóm :", Canvas_HAIRMOD_PROTECTED.hw - 60, Canvas_HAIRMOD_PROTECTED.hh + 15, 0);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, (seleted == 3) ? 1 : 0, 18, 11, 0, xC, yC, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			if (isAutoParty)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, 22, 18, 11, 0, xC, yC, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Tự về làng :", Canvas_HAIRMOD_PROTECTED.hw - 60, Canvas_HAIRMOD_PROTECTED.hh + 40, 0);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, (seleted == 4) ? 1 : 0, 18, 11, 0, xC, yC + 25, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			if (isAutoComeHome)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, 22, 18, 11, 0, xC, yC + 25, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
			tfHp.paint(g);
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			tfMp.paint(g);
			break;
		case 1:
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Đánh:", Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh - 80, MFont_HAIRMOD_PROTECTED.CENTER);
			g.setColor(16777215);
			g.drawRect(array[0] - 16, Canvas_HAIRMOD_PROTECTED.hh - 60, 32, 32);
			g.drawRect(Canvas_HAIRMOD_PROTECTED.hw - 16, Canvas_HAIRMOD_PROTECTED.hh - 60, 32, 32);
			g.drawRect(array[2] - 16, Canvas_HAIRMOD_PROTECTED.hh - 60, 32, 32);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Hổ trợ:", Canvas_HAIRMOD_PROTECTED.hw - 15, Canvas_HAIRMOD_PROTECTED.hh - 20, MFont_HAIRMOD_PROTECTED.LEFT);
			g.drawRect(array[0] - 16, Canvas_HAIRMOD_PROTECTED.hh - 3, 32, 32);
			g.drawRect(Canvas_HAIRMOD_PROTECTED.hw - 16, Canvas_HAIRMOD_PROTECTED.hh - 3, 32, 32);
			g.drawRect(array[2] - 16, Canvas_HAIRMOD_PROTECTED.hh - 3, 32, 32);
			g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 140, Canvas_HAIRMOD_PROTECTED.hh + 43, 280, 1);
			paintSkillAuto(g, array[0] - 32, Canvas_HAIRMOD_PROTECTED.hh + 68);
			break;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
		if (countL > 0)
		{
			countL--;
		}
		if (countR > 0)
		{
			countR--;
		}
		if (isOptionFocus)
		{
			Res_HAIRMOD_PROTECTED.paintDlgFull(g, Canvas_HAIRMOD_PROTECTED.w / 2 - 80, Canvas_HAIRMOD_PROTECTED.h / 2 - 80, 160, 160);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgBar, Canvas_HAIRMOD_PROTECTED.w / 2 - 65 - countL, Canvas_HAIRMOD_PROTECTED.h / 2 - 66, 3);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgBar, 0, 0, 11, 7, 2, Canvas_HAIRMOD_PROTECTED.w / 2 + 65 + countR, Canvas_HAIRMOD_PROTECTED.h / 2 - 66, 3);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, optionFC[GameScr_HAIRMOD_PROTECTED.typeOptionFocus], Canvas_HAIRMOD_PROTECTED.w / 2, Canvas_HAIRMOD_PROTECTED.h / 2 - 73, MFont_HAIRMOD_PROTECTED.CENTER);
			for (int i = 0; i < info.Length; i++)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, info[i], Canvas_HAIRMOD_PROTECTED.w / 2 - 72, Canvas_HAIRMOD_PROTECTED.h / 2 - 50 + i * MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT, 0);
			}
			base.paint(g);
		}
		else
		{
			paintTabAuto(g);
			base.paint(g);
		}
	}

	private void paintSkillAuto(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		mScroll.setStyle(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.getnSkill(), 30, x, y - 15, 290, 30, false, 1);
		mScroll.setClip(g, Canvas_HAIRMOD_PROTECTED.hw - 145, y - 17, 290, 50);
		for (int i = 0; i < Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.getnSkill(); i++)
		{
			GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(i, i * 30 + x, y - 15, 0, 0, g);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgKhung, i * 30 + x, y - 15, 0);
			MFont_HAIRMOD_PROTECTED.smallFontColor[1].drawString(g, Char_HAIRMOD_PROTECTED.skillLevelLearnt[i] + string.Empty, i * 30 + x + 15, y + 5, MFont_HAIRMOD_PROTECTED.RIGHT);
			if (focus == i && Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3)
			{
				g.setColor(16388369);
				g.drawRect(i * 30 + x - 2, y - 17, 24, 24);
			}
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (IDSkillAuto != null)
		{
			for (int j = 0; j < IDSkillAuto.Length; j++)
			{
				if (IDSkillAuto[j] != -1)
				{
					GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(IDSkillAuto[j], vitri[j], Canvas_HAIRMOD_PROTECTED.hh - 54, 0, 0, g);
				}
			}
		}
		if (IDSkillBuff == null)
		{
			return;
		}
		for (int k = 0; k < IDSkillBuff.Length; k++)
		{
			if (IDSkillBuff[k] != -1)
			{
				GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(IDSkillBuff[k], vitri[k], Canvas_HAIRMOD_PROTECTED.hh + 4, 0, 0, g);
			}
		}
	}

	public bool isBuffSkillAuto()
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
			if (focus == array[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][i])
			{
				return true;
			}
		}
		return false;
	}

	private void doSelectedSkillAuto()
	{
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[focus] == -1)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin gặp Lâm tướng quân để học kỹ năng này");
			return;
		}
		bool flag = false;
		bool flag2 = isBuffSkillAuto();
		if (flag2)
		{
			flag = SkillManager_HAIRMOD_PROTECTED.SKILL_CAN_BUFF_TO_USER[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][focus - 4] == -1;
		}
		if (flag)
		{
			return;
		}
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[focus] > 0)
		{
			if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz != 2)
			{
				doGiveSkillToQuickSlotAuto(focus, flag2);
			}
			else if (focus == 6)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể dùng skill này");
			}
			else
			{
				doGiveSkillToQuickSlotAuto(focus, flag2);
			}
		}
		else
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa học kỹ năng này");
		}
	}

	private void doGiveSkillToQuickSlotAuto(int skillType, bool isBuff)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < 3; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Phím số " + (1 + i * 2));
			command_HAIRMOD_PROTECTED.action = delegate
			{
				if (isBuff)
				{
					GameScr_HAIRMOD_PROTECTED.indexTabSlot = 1;
					IDSkillBuff[ii] = skillType;
				}
				else
				{
					GameScr_HAIRMOD_PROTECTED.indexTabSlot = 0;
					IDSkillAuto[ii] = skillType;
				}
				MainChar_HAIRMOD_PROTECTED.quickSlot[GameScr_HAIRMOD_PROTECTED.indexTabSlot][ii].setIsSkill(skillType, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz, isBuff);
				RMS_HAIRMOD_PROTECTED.saveQuickSlot();
				GameScr_HAIRMOD_PROTECTED.indexTabSlot = 0;
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
	}

	public override void pointerPress(int dx, int dy)
	{
		base.pointerPress(dx, dy);
	}
}
