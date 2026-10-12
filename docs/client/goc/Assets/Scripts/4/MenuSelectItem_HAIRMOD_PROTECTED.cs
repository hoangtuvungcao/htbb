public class MenuSelectItem_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public int width;

	public int height;

	public int x1;

	public int y1;

	public int x2;

	public int y2;

	public static Image_HAIRMOD_PROTECTED imgIChat;

	public static Image_HAIRMOD_PROTECTED imgIChange;

	public static Image_HAIRMOD_PROTECTED imgINapxu;

	public static Image_HAIRMOD_PROTECTED imgIDosat;

	public static Image_HAIRMOD_PROTECTED imgIXuongngua;

	public static Image_HAIRMOD_PROTECTED imgIDangxuat;

	public static Image_HAIRMOD_PROTECTED imgIThoat;

	public static Image_HAIRMOD_PROTECTED imgWap36;

	public static Image_HAIRMOD_PROTECTED imgWap;

	public static MenuSelectItem_HAIRMOD_PROTECTED me;

	private int xB = 20;

	private int ww;

	private int wT;

	private Command_HAIRMOD_PROTECTED cmdChat;

	private int selected;

	public static bool isClose;

	public MenuSelectItem_HAIRMOD_PROTECTED()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		right = new Command_HAIRMOD_PROTECTED();
		right.action = action;
		if (imgWap == null)
		{
			imgWap = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/wap");
			imgWap36 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/wap36");
		}
	}

	public static MenuSelectItem_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new MenuSelectItem_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		base.switchToMe();
		init();
	}

	public override void init()
	{
		width = (height = 40);
		y1 = Canvas_HAIRMOD_PROTECTED.h - 160;
		y2 = Canvas_HAIRMOD_PROTECTED.h - 50;
		cmdChat = new Command_HAIRMOD_PROTECTED();
		ActionChat_HAIRMOD_PROTECTED actionChat = delegate(string str)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.doChatFromMe(str);
		};
		cmdChat.actionChat = actionChat;
		ww = imgIChange.getWidth();
		wT = 8 * ww + 7 * (ww / 2);
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.update();
		y1 += 4;
		if (y1 > Canvas_HAIRMOD_PROTECTED.h - 130)
		{
			y1 = Canvas_HAIRMOD_PROTECTED.h - 130;
		}
		y2 -= 4;
		if (y2 < Canvas_HAIRMOD_PROTECTED.h - 80)
		{
			y2 = Canvas_HAIRMOD_PROTECTED.h - 80;
		}
		if (Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointer(xB + (ww + ww / 2), Canvas_HAIRMOD_PROTECTED.h - 130, wT, 90))
		{
			int num = (Canvas_HAIRMOD_PROTECTED.px - (xB + (ww + ww / 2))) / (ww + ww / 2);
			selected = num;
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pxLast - Canvas_HAIRMOD_PROTECTED.px) < 10 && Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pyLast - Canvas_HAIRMOD_PROTECTED.py) < 10)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				switch (selected)
				{
				case 0:
				{
					if (!Main_HAIRMOD_PROTECTED.isPC)
					{
						ipKeyboard_HAIRMOD_PROTECTED.openKeyBoard("chat", ipKeyboard_HAIRMOD_PROTECTED.TEXT, string.Empty, cmdChat);
						Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
						break;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
					Canvas_HAIRMOD_PROTECTED.gameScr.chatMode = true;
					Canvas_HAIRMOD_PROTECTED.endDlg();
					ActionPerform_HAIRMOD_PROTECTED ok = delegate
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.doChatFromMe(Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
						Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.setText(string.Empty);
					};
					Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nội dung Chat", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 100, false);
					Canvas_HAIRMOD_PROTECTED.inputDlg.show();
					break;
				}
				case 1:
					GameService_HAIRMOD_PROTECTED.gI().setConfig(6);
					Canvas_HAIRMOD_PROTECTED.endDlg();
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.posTransRoad = null;
					break;
				case 2:
					Canvas_HAIRMOD_PROTECTED.gameScr.doNapXu();
					Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
					break;
				case 3:
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.requestKiller();
					Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
					break;
				case 4:
					Canvas_HAIRMOD_PROTECTED.gameScr.unRideHorse();
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.posTransRoad = null;
					break;
				case 5:
					if (Tilemap_HAIRMOD_PROTECTED.lv == 0 || Tilemap_HAIRMOD_PROTECTED.lv == 201 || Tilemap_HAIRMOD_PROTECTED.lv == 70 || Tilemap_HAIRMOD_PROTECTED.lv == 80)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể bật auto ở đây.");
						break;
					}
					Canvas_HAIRMOD_PROTECTED.autoTab.switchToMe();
					Canvas_HAIRMOD_PROTECTED.clearKeyHold();
					Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
					return;
				case 6:
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.instance.init();
						Session_ME_HAIRMOD_PROTECTED.gI().close();
						Tilemap_HAIRMOD_PROTECTED.loadMap(0, null);
						ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
						Main_HAIRMOD_PROTECTED.isStartSaveIp = false;
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					ActionPerform_HAIRMOD_PROTECTED noAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn chọn lại máy chủ không ?", yesAction, noAction);
					break;
				}
				case 7:
					if (GameMidlet_HAIRMOD_PROTECTED.url != string.Empty)
					{
						GameMidlet_HAIRMOD_PROTECTED.requestLink(GameMidlet_HAIRMOD_PROTECTED.url);
					}
					break;
				}
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			if (Main_HAIRMOD_PROTECTED.canvas.canPutKey())
			{
				right.perform();
				isClose = true;
			}
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
		}
		base.update();
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
		Image_HAIRMOD_PROTECTED[] array = new Image_HAIRMOD_PROTECTED[8]
		{
			imgIChat,
			imgIChange,
			imgINapxu,
			imgIDosat,
			imgIXuongngua,
			Res_HAIRMOD_PROTECTED.icon_automode,
			imgIDangxuat,
			imgWap
		};
		for (int i = 0; i < 8; i++)
		{
			g.drawImage(array[i], xB + (i + 1) * (ww + ww / 2), y1, 0);
		}
		base.paint(g);
	}
}
