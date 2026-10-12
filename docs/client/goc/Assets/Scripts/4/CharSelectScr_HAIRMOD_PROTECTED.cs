public class CharSelectScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static CharSelectScr_HAIRMOD_PROTECTED me;

	public MainChar_HAIRMOD_PROTECTED[] charInfos;

	private MainChar_HAIRMOD_PROTECTED[] chars;

	public int selected;

	public Command_HAIRMOD_PROTECTED cmdSelect;

	public Command_HAIRMOD_PROTECTED cmdNew;

	public Command_HAIRMOD_PROTECTED cmdDel;

	public Command_HAIRMOD_PROTECTED cmdClose;

	public static Image_HAIRMOD_PROTECTED imgBg;

	public static CharSelectScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new CharSelectScr_HAIRMOD_PROTECTED());
	}

	public override bool keyPress(int keyCode)
	{
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		ServerListScr_HAIRMOD_PROTECTED.gI().paintBg(g);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, LoginScr_HAIRMOD_PROTECTED.nameServer, 4, 2, 0);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Chọn nhân vật:", Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh - 65, 2);
		int num = Canvas_HAIRMOD_PROTECTED.hw - 100;
		int num2 = Canvas_HAIRMOD_PROTECTED.hh - 30;
		int num3 = 0;
		int num4 = num;
		while (num3 < 3)
		{
			g.setColor(1379840);
			g.fillRect(num4 + 2, num2 - 11, 36, 86);
			if (num3 == selected)
			{
				g.setColor(15769344);
				if (num3 < charInfos.Length)
				{
					g.setColor((charInfos.Length == 0) ? 15769344 : ((charInfos[num3].isusing != 1) ? 2205861 : 15769344));
				}
			}
			else
			{
				g.setColor(10644224);
				if (num3 < charInfos.Length)
				{
					g.setColor((charInfos.Length == 0) ? 10644224 : ((charInfos[num3].isusing != 1) ? 34949 : 10644224));
				}
			}
			g.fillRect(num4 + 3, num2 - 10, 34, 84);
			if (num3 < chars.Length)
			{
				chars[num3].x = (short)(num4 + 20);
				chars[num3].y = (short)(num2 + 64);
				chars[num3].paintAvatar(g, chars[num3].x, chars[num3].y);
			}
			num3++;
			num4 += 83;
		}
		if (chars != null && charInfos != null && chars.Length != 0 && selected < chars.Length && charInfos.Length != 0)
		{
			short num5 = 0;
			num5 = (short)((charInfos[selected].lastLv <= 0) ? 1 : charInfos[selected].lastLv);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, charInfos[selected].name + " lv: " + num5, Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh + 55, 2);
			if (charInfos[selected].isusing == 0)
			{
				MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Còn lại: " + charInfos[selected].day + " ngày", Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh + 58, 2);
			}
			if (charInfos[selected].nationID != -1)
			{
				int width = MFont_HAIRMOD_PROTECTED.borderFont.getWidth(charInfos[selected].name + " lv: " + num5);
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNation, 0, charInfos[selected].nationID * 11, 11, 11, 0, Canvas_HAIRMOD_PROTECTED.hw - width / 2 - 15, Canvas_HAIRMOD_PROTECTED.hh + 60, 20);
			}
		}
		base.paint(g);
	}

	public override void pointerPress(int dx, int dy)
	{
		base.pointerPress(dx, dy);
	}

	public override void switchToMe()
	{
		base.switchToMe();
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.loginScr.updateCamera();
		for (int i = 0; i < chars.Length; i++)
		{
			chars[i].update();
		}
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(4))
		{
			selected--;
			flag = true;
			if (selected < 0)
			{
				selected = 2;
			}
			if (charInfos.Length != 0)
			{
				if (selected < charInfos.Length)
				{
					if (charInfos[selected].isusing == 1)
					{
						cmdDel.caption = "Xóa";
					}
					else
					{
						cmdDel.caption = "Khôi phục";
					}
				}
				else
				{
					cmdDel.caption = string.Empty;
				}
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(6))
		{
			selected++;
			flag = true;
			if (selected > 2)
			{
				selected = 0;
			}
			if (charInfos.Length != 0)
			{
				if (selected < charInfos.Length)
				{
					if (charInfos[selected].isusing == 1)
					{
						cmdDel.caption = "Xóa";
					}
					else
					{
						cmdDel.caption = "Khôi phục";
					}
				}
				else
				{
					cmdDel.caption = string.Empty;
				}
			}
		}
		int num = -1;
		if (Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 40, 200, 84) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			int num2 = Canvas_HAIRMOD_PROTECTED.hw - 100;
			flag = true;
			num = (Canvas_HAIRMOD_PROTECTED.px - num2) / 60;
			if (num > 2)
			{
				num = 2;
			}
			if (num < 0)
			{
				num = 0;
			}
			if (selected != num)
			{
				selected = num;
				if (charInfos.Length != 0)
				{
					if (selected < charInfos.Length)
					{
						if (charInfos[selected].isusing == 1)
						{
							cmdDel.caption = "Xóa";
						}
						else
						{
							cmdDel.caption = "Khôi phục";
						}
					}
					else
					{
						cmdDel.caption = string.Empty;
					}
				}
			}
			else
			{
				doSelectChar();
				flag = false;
			}
		}
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			if (flag)
			{
				if (selected < chars.Length)
				{
					center = cmdSelect;
					left = cmdDel;
				}
				else
				{
					CreateCharScr_HAIRMOD_PROTECTED.gI().switchToMe();
					center = null;
					left = null;
				}
			}
		}
		else if (flag)
		{
			if (selected < chars.Length)
			{
				center = cmdSelect;
				left = cmdDel;
			}
			else
			{
				center = cmdNew;
				left = null;
			}
			if (num == selected)
			{
				center.perform();
			}
			if (num < 3 && num >= 0)
			{
				selected = num;
			}
		}
		base.update();
	}

	public void doSelectChar()
	{
		if (selected >= 0 && selected <= charInfos.Length - 1 && charInfos[selected].isusing != 0)
		{
			Canvas_HAIRMOD_PROTECTED.startWaitDlg("Xin chờ..", true);
			GameService_HAIRMOD_PROTECTED.gI().selectChar(charInfos[selected].IDDB, 1);
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.imgWpa = chars[selected].imgWpa;
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.dxWear = chars[selected].dxWear;
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.dyWear = chars[selected].dyWear;
			ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
			chars = null;
			me = null;
		}
	}

	public override void updateKey()
	{
		base.updateKey();
	}

	public void setCharList(MainChar_HAIRMOD_PROTECTED[] charInfos)
	{
		if (!Session_ME_HAIRMOD_PROTECTED.connected)
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.loginScr != null)
		{
			Canvas_HAIRMOD_PROTECTED.loginScr.savePass();
		}
		if (!Canvas_HAIRMOD_PROTECTED.gameScr.isNewVersionAvailable)
		{
			Canvas_HAIRMOD_PROTECTED.endDlg();
		}
		this.charInfos = charInfos;
		chars = new MainChar_HAIRMOD_PROTECTED[charInfos.Length];
		chars = charInfos;
		cmdSelect = new Command_HAIRMOD_PROTECTED("Chọn");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doSelectChar();
		};
		cmdSelect.action = action;
		cmdNew = new Command_HAIRMOD_PROTECTED("Tạo mới");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			CreateCharScr_HAIRMOD_PROTECTED.gI().switchToMe();
		};
		cmdNew.action = action2;
		cmdDel = new Command_HAIRMOD_PROTECTED("Xóa");
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			if (cmdDel.caption.Equals("Xóa"))
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startWaitDlg("Xin chờ..", true);
					GameService_HAIRMOD_PROTECTED.gI().selectChar(this.charInfos[selected].IDDB, 2);
				};
				ActionPerform_HAIRMOD_PROTECTED noAction = delegate
				{
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn thật sự muốn xóa nhân vật này?", yesAction, noAction);
			}
			else if (cmdDel.caption.Equals("Khôi phục"))
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startWaitDlg("Xin chờ..", true);
					GameService_HAIRMOD_PROTECTED.gI().selectChar(this.charInfos[selected].IDDB, 3);
				};
				ActionPerform_HAIRMOD_PROTECTED noAction2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn muốn khôi phục nhân vật này?", yesAction2, noAction2);
			}
		};
		cmdDel.action = action3;
		ActionPerform_HAIRMOD_PROTECTED action4 = delegate
		{
			Session_ME_HAIRMOD_PROTECTED.gI().close();
			ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
			Main_HAIRMOD_PROTECTED.isStartSaveIp = false;
		};
		cmdClose = new Command_HAIRMOD_PROTECTED("Thoát");
		cmdClose.action = action4;
		if (selected < chars.Length)
		{
			center = cmdSelect;
			left = cmdDel;
		}
		else
		{
			center = cmdNew;
			left = null;
		}
		right = cmdClose;
		if (charInfos.Length == 0)
		{
			return;
		}
		if (selected >= 0 && selected < charInfos.Length)
		{
			if (charInfos[selected].isusing == 1)
			{
				cmdDel.caption = "Xóa";
			}
			else
			{
				cmdDel.caption = "Khôi phục";
			}
		}
		else
		{
			selected = 0;
		}
	}
}
