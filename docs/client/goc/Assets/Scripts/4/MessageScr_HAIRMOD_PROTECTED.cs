public class MessageScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static MessageScr_HAIRMOD_PROTECTED me;

	protected string[] info;

	private mVector_HAIRMOD_PROTECTED list;

	private int focusTab = 1;

	private MyScreen_HAIRMOD_PROTECTED lastScr;

	private int wTab = 60;

	private new int hTab = 20;

	private int du = 10;

	private int w;

	private int h;

	private int x;

	private int y;

	private int xTr;

	private int yTr;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	public static int selected;

	public static int lastSe;

	public static int limit;

	public static long chatDelay;

	private ChatTab_HAIRMOD_PROTECTED focusT;

	private ChatTab_HAIRMOD_PROTECTED publicTab;

	private TField_HAIRMOD_PROTECTED tfInput;

	public static sbyte nMSG;

	private bool trans;

	private int pa;

	private int a;

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

	public MessageScr_HAIRMOD_PROTECTED()
	{
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Menu");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			if (focusTab != 0)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Đóng tab");
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
					list.removeElementAt(focusTab);
					if (focusTab >= list.size())
					{
						focusTab = list.size() - 1;
					}
					setWTab();
					changTab();
				};
				command_HAIRMOD_PROTECTED2.action = action2;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Đóng");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				lastScr.switchToMe();
			};
			command_HAIRMOD_PROTECTED3.action = action3;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
		};
		command_HAIRMOD_PROTECTED.action = action;
		left = command_HAIRMOD_PROTECTED;
		tfInput = new TField_HAIRMOD_PROTECTED();
		init();
		tfInput.isFocus = true;
		list = new mVector_HAIRMOD_PROTECTED();
		publicTab = new ChatTab_HAIRMOD_PROTECTED("Tin đến", null, null, false);
		focusTab = 0;
		publicTab.text = new mVector_HAIRMOD_PROTECTED();
		list.addElement(publicTab);
		changTab();
		setWTab();
	}

	public static MessageScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new MessageScr_HAIRMOD_PROTECTED());
	}

	public override bool keyPress(int keyCode)
	{
		if (focusT.isInput && tfInput.isFocus && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			tfInput.keyPressed(keyCode);
		}
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.translate(-g.getTranslateX(), -g.getTranslateY());
		lastScr.paint(g);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		base.paint(g);
		paintTab(g, x + xTr, y + yTr, w, h);
		paintText(g);
	}

	public override void pointerPress(int dx, int dy)
	{
		base.pointerPress(dx, dy);
	}

	public override void switchToMe()
	{
		lastScr = Canvas_HAIRMOD_PROTECTED.currentScreen;
		changTab();
		nMSG = 0;
		chatDelay = (int)mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100;
		base.switchToMe();
		init();
	}

	public override void update()
	{
		lastScr.update();
		updateKeyShowText();
	}

	public override void updateKey()
	{
		if (focusT.isInput && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			tfInput.update();
		}
		if (Canvas_HAIRMOD_PROTECTED.py <= 100 + yTr && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			if (Canvas_HAIRMOD_PROTECTED.px < Canvas_HAIRMOD_PROTECTED.hw)
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = true;
				Out_HAIRMOD_PROTECTED.println("44444");
			}
			else if (Canvas_HAIRMOD_PROTECTED.px > Canvas_HAIRMOD_PROTECTED.hw)
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = true;
				Out_HAIRMOD_PROTECTED.println("6666");
			}
		}
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(4))
		{
			focusTab--;
			if (focusTab < 0)
			{
				focusTab = list.size() - 1;
			}
			changTab();
		}
		else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(6))
		{
			focusTab++;
			if (focusTab >= list.size())
			{
				focusTab = 0;
			}
			changTab();
		}
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
			if (cmy < cmyLim)
			{
				selected++;
			}
			if (selected > focusT.text.size() - lastSe)
			{
				selected = focusT.text.size() - lastSe;
			}
			flag = true;
		}
		if (flag)
		{
			cmtoY = selected * 14 - limit / 2;
			if (cmtoY < 0)
			{
				cmtoY = 0;
			}
			if (cmtoY > cmyLim)
			{
				cmtoY = cmyLim;
			}
		}
		xBegint = 20 + xTr;
		yBegint = 20 + yTr;
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
		wToucht = num - 40;
		hToucht = num2 - 60;
		base.updateKey();
	}

	public void moveCameraY()
	{
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
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
		w = num - 48;
		h = num2 - 64;
		if (Canvas_HAIRMOD_PROTECTED.w * Canvas_HAIRMOD_PROTECTED.h > 153600)
		{
			xTr = (Canvas_HAIRMOD_PROTECTED.w - num) / 2;
			yTr = (Canvas_HAIRMOD_PROTECTED.h - num2) / 2;
		}
		x = (y = 24);
		tfInput.x = x + 4 + xTr;
		tfInput.y = y + h - 25 + yTr;
		tfInput.height = 20;
		tfInput.width = w - 8;
		setWTab();
	}

	public void setWTab()
	{
		if (list != null && focusT != null)
		{
			du = 20;
			wTab = w - du * list.size() + du - 1;
			if (wTab < MFont_HAIRMOD_PROTECTED.borderFont.getWidth(focusT.name) + 15)
			{
				wTab = MFont_HAIRMOD_PROTECTED.borderFont.getWidth(focusT.name) + 15;
				du = (w - wTab) / (list.size() - 1);
			}
			if (list.size() == 1)
			{
				wTab = w - 1;
			}
		}
	}

	public void setFocusTab(string name)
	{
		for (int i = 0; i < list.size(); i++)
		{
			ChatTab_HAIRMOD_PROTECTED chatTab_HAIRMOD_PROTECTED = (ChatTab_HAIRMOD_PROTECTED)list.elementAt(i);
			if (chatTab_HAIRMOD_PROTECTED.name.Equals(name))
			{
				chatTab_HAIRMOD_PROTECTED.isOpen = false;
				focusT = chatTab_HAIRMOD_PROTECTED;
				focusTab = i;
			}
		}
	}

	public Command_HAIRMOD_PROTECTED setCmd(string cation, ActionPerform_HAIRMOD_PROTECTED action0)
	{
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(cation);
		command_HAIRMOD_PROTECTED.action = action0;
		return command_HAIRMOD_PROTECTED;
	}

	public void addTab(string te, string name, int type)
	{
		ChatTab_HAIRMOD_PROTECTED chatTab_HAIRMOD_PROTECTED = null;
		if (name != null && !name.Equals(publicTab.name))
		{
			chatTab_HAIRMOD_PROTECTED = getTab(name);
			if (this != Canvas_HAIRMOD_PROTECTED.currentScreen)
			{
				nMSG = 1;
			}
		}
		else
		{
			chatTab_HAIRMOD_PROTECTED = publicTab;
		}
		if (chatTab_HAIRMOD_PROTECTED == null)
		{
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				doChat();
			};
			Command_HAIRMOD_PROTECTED cen = setCmd("Chat", action);
			chatTab_HAIRMOD_PROTECTED = new ChatTab_HAIRMOD_PROTECTED(name, cen, tfInput.cmdClear, true);
			chatTab_HAIRMOD_PROTECTED.text = new mVector_HAIRMOD_PROTECTED();
			list.addElement(chatTab_HAIRMOD_PROTECTED);
			setWTab();
		}
		if (!te.Equals(string.Empty))
		{
			string[] array = MFont_HAIRMOD_PROTECTED.arialFontW.splitFontBStrInLine(te, w - 20);
			for (int i = 0; i < array.Length; i++)
			{
				chatTab_HAIRMOD_PROTECTED.text.addElement(array[i]);
				if (chatTab_HAIRMOD_PROTECTED.text.size() > 50)
				{
					chatTab_HAIRMOD_PROTECTED.text.removeElementAt(0);
				}
			}
		}
		if (chatTab_HAIRMOD_PROTECTED == focusT)
		{
			if (selected + 1 == focusT.text.size() - lastSe)
			{
				selected = focusT.text.size() - lastSe;
				cmtoY = selected * 14 - limit / 2;
			}
			cmyLim = focusT.text.size() * 14 - limit + 50;
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
		}
	}

	protected void doChat()
	{
		if (!tfInput.getText().Equals(string.Empty))
		{
			chatDelay = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100;
			if (focusT.name.Equals("Bang hội"))
			{
				GameService_HAIRMOD_PROTECTED.gI().chatToClan(tfInput.getText());
			}
			else
			{
				addTab(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name + ": " + tfInput.getText(), focusT.name, 0);
				GameService_HAIRMOD_PROTECTED.gI().sendMsgPrivate(focusT.name, tfInput.getText());
			}
			tfInput.setText(string.Empty);
		}
	}

	private ChatTab_HAIRMOD_PROTECTED getTab(string name)
	{
		for (int i = 0; i < list.size(); i++)
		{
			ChatTab_HAIRMOD_PROTECTED chatTab_HAIRMOD_PROTECTED = (ChatTab_HAIRMOD_PROTECTED)list.elementAt(i);
			if (chatTab_HAIRMOD_PROTECTED.name.Equals(name))
			{
				return chatTab_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	private void changTab()
	{
		focusT = (ChatTab_HAIRMOD_PROTECTED)list.elementAt(focusTab);
		focusT.isOpen = false;
		limit = h - hTab;
		if (focusT.isInput)
		{
			limit -= 21;
		}
		cmyLim = focusT.text.size() * 14 - limit + 50;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		cmy = (cmtoY = 0);
		lastSe = (limit - 10) / 2 / 14;
		selected = lastSe;
		center = focusT.center;
		right = focusT.right;
	}

	private void paintText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, focusT.name, x + wTab / 2 + focusTab * du + xTr, y + hTab / 2 - 6 + yTr, 2);
		if (focusT.isInput && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			tfInput.paint(g);
		}
		g.setClip(x + xTr, y + hTab + 5 + yTr, w - 3, limit - 9);
		g.translate(xTr, -cmy + yTr);
		if (focusT.text.size() <= 0)
		{
			return;
		}
		int num = cmy / 14;
		if (num <= 0)
		{
			num = 0;
		}
		int num2 = num + limit / 14 + 1;
		if (num2 >= focusT.text.size())
		{
			num2 = focusT.text.size();
		}
		for (int i = num; i < num2; i++)
		{
			string text = (string)focusT.text.elementAt(i);
			if (text != null)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, text, x + 8, y + i * 14 + hTab + 3, 0);
			}
		}
	}

	private void paintTab(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h)
	{
		g.setColor(277044);
		g.fillRect(x, y + hTab, w, h - hTab);
		for (int i = 0; i < 3; i++)
		{
			g.setColor(Res_HAIRMOD_PROTECTED.color[i]);
			g.drawRect(x + i, y + i + hTab, w - i * 2 - 1, h - i * 2 - hTab - 1);
		}
		for (int num = list.size() - 1; num > focusTab; num--)
		{
			ChatTab_HAIRMOD_PROTECTED chatTab_HAIRMOD_PROTECTED = (ChatTab_HAIRMOD_PROTECTED)list.elementAt(num);
			for (int j = 0; j < 3; j++)
			{
				g.setColor(Res_HAIRMOD_PROTECTED.color[j]);
				g.drawRect(x + j + num * du, y + j, wTab - j * 2, hTab - j * 2);
			}
			chatTab_HAIRMOD_PROTECTED.count++;
			if (chatTab_HAIRMOD_PROTECTED.count > 10)
			{
				chatTab_HAIRMOD_PROTECTED.count = 0;
			}
			if (chatTab_HAIRMOD_PROTECTED.isOpen && chatTab_HAIRMOD_PROTECTED.count >= 5)
			{
				g.setColor(573098);
				g.fillRect(x + 3 + du * num, y + 3, wTab - 5, hTab - 3);
			}
			else
			{
				g.setClip(x + du * num + wTab - du, y, du - 2, hTab);
				g.drawImage(Res_HAIRMOD_PROTECTED.imgInv[2], x + du * num + 3 + wTab - 70, y + 3, 0);
				g.setClip(x, y, w, h);
			}
		}
		for (int k = 0; k <= focusTab; k++)
		{
			for (int l = 0; l < 3; l++)
			{
				g.setColor(Res_HAIRMOD_PROTECTED.color[l]);
				g.drawRect(x + l + k * du, y + l, wTab - l * 2, hTab - l * 2);
			}
			ChatTab_HAIRMOD_PROTECTED chatTab_HAIRMOD_PROTECTED2 = (ChatTab_HAIRMOD_PROTECTED)list.elementAt(k);
			chatTab_HAIRMOD_PROTECTED2.count++;
			if (chatTab_HAIRMOD_PROTECTED2.count > 10)
			{
				chatTab_HAIRMOD_PROTECTED2.count = 0;
			}
			if (chatTab_HAIRMOD_PROTECTED2.isOpen && chatTab_HAIRMOD_PROTECTED2.count >= 5)
			{
				g.setColor(573098);
				g.fillRect(x + 3 + du * k, y + 3, wTab - 5, hTab - 3 + ((k == focusTab) ? 4 : 0));
			}
			else if (k == focusTab)
			{
				g.setClip(x + du * k + 3, y, wTab - 5, hTab + 3);
				for (int m = 0; m < wTab / 70 + 1; m++)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgInv[2], x + 3 + du * k + 70 * m, y + 3, 0);
				}
			}
			else
			{
				g.setClip(x + du * k, y, du, hTab);
				g.drawImage(Res_HAIRMOD_PROTECTED.imgInv[2], x + du * k + 3, y + 3, 0);
			}
			g.setClip(x - 4, y - 4, w + 8, h + 9);
		}
		g.drawImage(Res_HAIRMOD_PROTECTED.imgInv[0], x - 2 + du * focusTab, y - 2, 0);
		g.drawRegion(Res_HAIRMOD_PROTECTED.imgInv[0], 0, 0, 18, 19, 2, x + 3 + wTab + du * focusTab, y - 2, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
		for (int n = 0; n < 2; n++)
		{
			for (int num2 = 0; num2 < 2; num2++)
			{
				g.setColor(Res_HAIRMOD_PROTECTED.color[(n != 0) ? (2 - num2) : (1 + num2)]);
				g.fillRect(x + focusTab * du + (num2 + 1) + (wTab - 3) * n, y + hTab - 8 + 6 + ((n != 0) ? (1 - num2) : num2), 1, 3);
			}
		}
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		g.drawRegion(Res_HAIRMOD_PROTECTED.imgInv3, 0, 0, 18, 19, 2, x - 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		g.drawImage(Res_HAIRMOD_PROTECTED.imgInv3, x + w + 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
	}

	public void updateKeyShowText()
	{
		countShowText++;
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownText && Canvas_HAIRMOD_PROTECTED.isPointer(xBegint, yBegint, wToucht, hToucht))
		{
			pyLastt = Canvas_HAIRMOD_PROTECTED.pyLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownText = false;
			timeDelayt = countShowText;
			pa = cmy;
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
			if (cmy < 0 || cmy > cmyLim)
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
			cmy += (int)(vYT / 15f);
			cmtoY = cmy;
			vYT -= vYT / 20f;
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
